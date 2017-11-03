package accessiblesolutions.accessiblescheduling.domain;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.cloudfoundry.samples.music.Application;
import org.cloudfoundry.samples.music.web.ShiftController;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.mongodb.BasicDBObject;
public class TestB {
	public TestB(){}
	private MongoOperations mongoOperation;

	@Before
    public void beforeEach() throws Exception {
	    	//ApplicationContext ctx = new AnnotationConfigApplicationContext(Application.class);
	    	//mongoOperation = (MongoOperations)ctx.getBean("mongoTemplate");
    }

//    @Test
//    public void shouldCreateNewObjectInEmbeddedMongoDb() {
//        // given
//    	BasicDBObject object = new BasicDBObject("testDoc", new Date());
//        // when
//        mongoOperation.save(object);
//
//        // then
//        assertTrue(mongoOperation.findAll(BasicDBObject.class).size()==1);
//    }

	@Test
	public void test() {
		assertTrue(true);
	}

	@Test
	public void shiftTestSample(){
		Shift shift = new Shift();
		assertTrue(shift!=null);
		assertFalse(shift.getAssigned());
		assertTrue(shift.getStartDate()==null);
	}
}
