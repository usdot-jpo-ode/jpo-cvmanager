package us.dot.its.jpo.ode.api.tasks;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import us.dot.its.jpo.ode.api.controllers.LiveFeedController;
import us.dot.its.jpo.ode.api.models.LiveFeedSessionIndex;



@Component
public class TestTask {

    @Scheduled(fixedRate = 5000)
    public void generateMonthlyReports() {
		System.out.println("Sending Message on Web Socket");

        LiveFeedSessionIndex index = new LiveFeedSessionIndex(12109, "-1");

        LiveFeedController.broadcast(index, "Sending Update");
	}


}

