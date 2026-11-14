package accessiblescheduling.repositories.inmemory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.Repository;

import accessiblescheduling.domain.AccessRequest;
import accessiblescheduling.domain.Client;
import accessiblescheduling.domain.ClientRequest;
import accessiblescheduling.domain.CustomField;
import accessiblescheduling.domain.CustomFieldData;
import accessiblescheduling.domain.Employee;
import accessiblescheduling.domain.ScheduleStatus;
import accessiblescheduling.domain.Session;
import accessiblescheduling.domain.Shift;
import accessiblescheduling.repositories.mongodb.MongoAccessRequestRepository;
import accessiblescheduling.repositories.mongodb.MongoClientRepository;
import accessiblescheduling.repositories.mongodb.MongoClientRequestRepository;
import accessiblescheduling.repositories.mongodb.MongoCustomFieldDataRepository;
import accessiblescheduling.repositories.mongodb.MongoCustomFieldRepository;
import accessiblescheduling.repositories.mongodb.MongoEmployeeRepository;
import accessiblescheduling.repositories.mongodb.MongoSessionRepository;
import accessiblescheduling.repositories.mongodb.MongoShiftRepository;
import accessiblescheduling.repositories.mongodb.MongoUpdateInfoRepository;
import accessiblescheduling.repositories.mongodb.ScheduleStatusRepository;
import accessiblescheduling.to.UpdateInfo;

@Configuration
@Profile("in-memory")
public class InMemoryRepositoryConfig {

    @Bean
    public MongoAccessRequestRepository mongoAccessRequestRepository() {
        return repository(MongoAccessRequestRepository.class, AccessRequest.class);
    }

    @Bean
    public MongoClientRepository mongoClientRepository() {
        return repository(MongoClientRepository.class, Client.class);
    }

    @Bean
    public MongoClientRequestRepository mongoClientRequestRepository() {
        return repository(MongoClientRequestRepository.class, ClientRequest.class);
    }

    @Bean
    public MongoCustomFieldDataRepository mongoCustomFieldDataRepository() {
        return repository(MongoCustomFieldDataRepository.class, CustomFieldData.class);
    }

    @Bean
    public MongoCustomFieldRepository mongoCustomFieldRepository() {
        return repository(MongoCustomFieldRepository.class, CustomField.class);
    }

    @Bean
    public MongoEmployeeRepository mongoEmployeeRepository() {
        return repository(MongoEmployeeRepository.class, Employee.class);
    }

    @Bean
    public MongoSessionRepository mongoSessionRepository() {
        return repository(MongoSessionRepository.class, Session.class);
    }

    @Bean
    public MongoShiftRepository mongoShiftRepository() {
        return repository(MongoShiftRepository.class, Shift.class);
    }

    @Bean
    public MongoUpdateInfoRepository mongoUpdateInfoRepository() {
        return repository(MongoUpdateInfoRepository.class, UpdateInfo.class);
    }

    @Bean
    public ScheduleStatusRepository scheduleStatusRepository() {
        return repository(ScheduleStatusRepository.class, ScheduleStatus.class);
    }

    @SuppressWarnings("unchecked")
    private <T extends Repository<?, ?>> T repository(Class<T> repositoryType, Class<?> entityType) {
        return (T) Proxy.newProxyInstance(
                repositoryType.getClassLoader(),
                new Class[] { repositoryType },
                new InMemoryRepositoryHandler(entityType));
    }

    private static class InMemoryRepositoryHandler implements InvocationHandler {
        private final Class<?> entityType;
        private final Map<String, Object> records = new LinkedHashMap<String, Object>();

        InMemoryRepositoryHandler(Class<?> entityType) {
            this.entityType = entityType;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            String name = method.getName();

            if ("toString".equals(name)) {
                return "InMemoryRepository<" + entityType.getSimpleName() + ">";
            }
            if ("hashCode".equals(name)) {
                return System.identityHashCode(proxy);
            }
            if ("equals".equals(name)) {
                return proxy == args[0];
            }

            if ("save".equals(name) || "insert".equals(name)) {
                return save(args[0]);
            }
            if ("findOne".equals(name)) {
                return records.get(String.valueOf(args[0]));
            }
            if ("exists".equals(name)) {
                return records.containsKey(String.valueOf(args[0]));
            }
            if ("findAll".equals(name)) {
                return findAll(args);
            }
            if ("count".equals(name)) {
                return Long.valueOf(records.size());
            }
            if ("delete".equals(name)) {
                delete(args);
                return null;
            }
            if ("deleteAll".equals(name)) {
                records.clear();
                return null;
            }
            if ("deleteByStartMonthAndStartYear".equals(name)) {
                return Long.valueOf(deleteByProperties(new String[] { "StartMonth", "StartYear" }, args));
            }
            if ("deleteByMonthAndYear".equals(name)) {
                deleteByProperties(new String[] { "Month", "Year" }, args);
                return null;
            }
            if (name.startsWith("findBy")) {
                return findBy(method, args);
            }

            throw new UnsupportedOperationException("Unsupported in-memory repository method: " + method);
        }

        private Object save(Object value) throws Exception {
            if (value instanceof Iterable) {
                List<Object> saved = new ArrayList<Object>();
                for (Object item : (Iterable<?>) value) {
                    saved.add(saveOne(item));
                }
                return saved;
            }
            return saveOne(value);
        }

        private Object saveOne(Object entity) throws Exception {
            String id = id(entity);
            if (id == null || id.length() == 0) {
                id = UUID.randomUUID().toString();
                setId(entity, id);
            }
            records.put(id, entity);
            return entity;
        }

        private Object findAll(Object[] args) {
            if (args == null || args.length == 0) {
                return new ArrayList<Object>(records.values());
            }
            if (args[0] instanceof Pageable) {
                return new PageImpl<Object>(new ArrayList<Object>(records.values()));
            }
            if (args[0] instanceof Iterable) {
                List<Object> matches = new ArrayList<Object>();
                for (Object id : (Iterable<?>) args[0]) {
                    Object match = records.get(String.valueOf(id));
                    if (match != null) {
                        matches.add(match);
                    }
                }
                return matches;
            }
            return new ArrayList<Object>(records.values());
        }

        private void delete(Object[] args) throws Exception {
            if (args == null || args.length == 0) {
                return;
            }
            Object target = args[0];
            if (target instanceof Iterable) {
                for (Object item : (Iterable<?>) target) {
                    delete(new Object[] { item });
                }
            } else if (entityType.isInstance(target)) {
                String id = id(target);
                if (id != null) {
                    records.remove(id);
                }
            } else {
                records.remove(String.valueOf(target));
            }
        }

        private Object findBy(Method method, Object[] args) throws Exception {
            String[] properties = method.getName().substring("findBy".length()).split("And");
            List<Object> matches = matches(properties, args);
            if (List.class.isAssignableFrom(method.getReturnType())) {
                return matches;
            }
            return matches.isEmpty() ? null : matches.get(0);
        }

        private long deleteByProperties(String[] properties, Object[] args) throws Exception {
            long deleted = 0;
            Iterator<Map.Entry<String, Object>> iterator = records.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, Object> entry = iterator.next();
                if (matches(entry.getValue(), properties, args)) {
                    iterator.remove();
                    deleted++;
                }
            }
            return deleted;
        }

        private List<Object> matches(String[] properties, Object[] args) throws Exception {
            List<Object> matches = new ArrayList<Object>();
            for (Object entity : records.values()) {
                if (matches(entity, properties, args)) {
                    matches.add(entity);
                }
            }
            return matches;
        }

        private boolean matches(Object entity, String[] properties, Object[] args) throws Exception {
            for (int index = 0; index < properties.length; index++) {
                Object actual = property(entity, properties[index]);
                Object expected = args[index];
                if (actual == null ? expected != null : !actual.equals(expected)) {
                    return false;
                }
            }
            return true;
        }

        private String id(Object entity) throws Exception {
            Object id = property(entity, "Id");
            if (id == null && entity instanceof ScheduleStatus) {
                ScheduleStatus status = (ScheduleStatus) entity;
                if (status.getMonth() != null && status.getYear() != null) {
                    id = status.getMonth() + "-" + status.getYear();
                }
            }
            return id == null ? null : String.valueOf(id);
        }

        private Object property(Object entity, String property) throws Exception {
            Method getter = entity.getClass().getMethod("get" + property);
            return getter.invoke(entity);
        }

        private void setId(Object entity, String id) throws Exception {
            try {
                Method setter = entity.getClass().getMethod("setId", String.class);
                setter.invoke(entity, id);
            } catch (NoSuchMethodException ignored) {
                // Some entities use another field as their identifier.
            }
        }
    }
}
