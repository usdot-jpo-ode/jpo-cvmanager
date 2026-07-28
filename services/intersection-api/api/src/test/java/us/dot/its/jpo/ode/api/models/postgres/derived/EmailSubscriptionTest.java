package us.dot.its.jpo.ode.api.models.postgres.derived;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import us.dot.its.jpo.ode.api.models.emails.UserEmailNotificationDto;

@ExtendWith(MockitoExtension.class)
public class EmailSubscriptionTest {

    @Test
    void testGetSubscribedTrue() {
        UserEmailNotificationDto sub1 = new UserEmailNotificationDto("category1", "desc1", "USER", true, false, false,
                false,
                        false, true, true, true, true, true);
        assertTrue(sub1.getSubscribed());

        sub1 = new UserEmailNotificationDto("category1", "desc1", "USER", false, true, false, false,
                        false, true, true, true, true, true);
        assertTrue(sub1.getSubscribed());

        sub1 = new UserEmailNotificationDto("category1", "desc1", "USER", false, false, true, false,
                        false, true, true, true, true, true);
        assertTrue(sub1.getSubscribed());

        sub1 = new UserEmailNotificationDto("category1", "desc1", "USER", false, false, false, true,
                        false, true, true, true, true, true);
        assertTrue(sub1.getSubscribed());

        sub1 = new UserEmailNotificationDto("category1", "desc1", "USER", false, false, false, false,
                        true, true, true, true, true, true);
        assertTrue(sub1.getSubscribed());
    }

    @Test
    void testGetSubscribedFalse() {
        UserEmailNotificationDto sub1 = new UserEmailNotificationDto("category1", "desc1", "USER", false, false, false,
                false,
                        false, true, true, true, true, true);
        assertFalse(sub1.getSubscribed());
    }

    @Test
    void testIsFrequencyEqualTrue() {
        // Both have no frequencies selected
        UserEmailNotificationDto sub1 = new UserEmailNotificationDto("category1", "desc1", "USER", false, false, false,
                false,
                        false, true, true, true, true, true);
        UserEmailNotificationDto sub2 = new UserEmailNotificationDto("category1", "desc1", "USER", false, false, false,
                false,
                        false, true, true, true, true, true);
        assertTrue(sub1.isFrequencyEqual(sub2));

        // Both have some frequencies selected
        sub1 = new UserEmailNotificationDto("category1", "desc1", "USER", true, false, false, false,
                        false, true, true, true, true, true);
        sub2 = new UserEmailNotificationDto("category1", "desc1", "USER", true, false, false, false,
                        false, true, true, true, true, true);
        assertTrue(sub1.isFrequencyEqual(sub2));

        // Both have all frequencies selected
        sub1 = new UserEmailNotificationDto("category1", "desc1", "USER", true, true, true, true,
                        true, true, true, true, true, true);
        sub2 = new UserEmailNotificationDto("category1", "desc1", "USER", true, true, true, true,
                        true, true, true, true, true, true);
        assertTrue(sub1.isFrequencyEqual(sub2));
    }

    @Test
    void testIsFrequencyEqualFalse() {
        // One has no frequencies selected
        UserEmailNotificationDto sub1 = new UserEmailNotificationDto("category1", "desc1", "USER", true, false, false,
                false,
                        false, true, true, true, true, true);
        UserEmailNotificationDto sub2 = new UserEmailNotificationDto("category1", "desc1", "USER", false, false, false,
                false,
                        false, true, true, true, true, true);
        assertFalse(sub1.isFrequencyEqual(sub2));

        // Both have different frequencies selected
        sub1 = new UserEmailNotificationDto("category1", "desc1", "USER", true, false, false, true,
                        false, true, true, true, true, true);
        sub2 = new UserEmailNotificationDto("category1", "desc1", "USER", true, false, true, false,
                        false, true, true, true, true, true);
        assertFalse(sub1.isFrequencyEqual(sub2));

        // Both have different frequencies selected
        sub1 = new UserEmailNotificationDto("category1", "desc1", "USER", false, true, true, true,
                        false, true, true, true, true, true);
        sub2 = new UserEmailNotificationDto("category1", "desc1", "USER", true, true, true, true,
                        true, true, true, true, true, true);
        assertFalse(sub1.isFrequencyEqual(sub2));
    }
}
