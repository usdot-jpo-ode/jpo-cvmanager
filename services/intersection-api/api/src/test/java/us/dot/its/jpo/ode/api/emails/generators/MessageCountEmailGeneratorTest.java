package us.dot.its.jpo.ode.api.emails.generators;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import us.dot.its.jpo.ode.api.SnapshotTestUtils;
import us.dot.its.jpo.ode.api.emails.EmailProperties;
import us.dot.its.jpo.ode.api.emails.UnsubscribeTokenGenerator;
import us.dot.its.jpo.ode.api.models.emails.EmailContent;
import us.dot.its.jpo.ode.api.models.emails.contents.message_counts.MessageCountCountsItem;
import us.dot.its.jpo.ode.api.models.emails.contents.message_counts.MessageCountEmailContents;
import us.dot.its.jpo.ode.api.models.emails.contents.message_counts.MessageCountRsuItem;

@ExtendWith(MockitoExtension.class)
class MessageCountEmailGeneratorTest {

    @Mock
    private UnsubscribeTokenGenerator unsubscribeTokenGenerator;

    @Mock
    private EmailProperties emailProperties;

    @Mock
    private TemplateEngine templateEngine;

    private MessageCountEmailGenerator messageCountEmailGenerator;

    @Test
    void testGenerateEmailBody_SnapshotTest() throws IOException {
        when(emailProperties.getCvmgrFrontEndUri()).thenReturn("https://cvmanager.com");

        // Configure the template resolver
        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setPrefix("templates/"); // Path to your templates directory
        templateResolver.setSuffix(".html"); // Template file extension
        templateResolver.setTemplateMode("HTML");
        templateResolver.setCharacterEncoding("UTF-8");

        // Configure the SpringTemplateEngine
        SpringTemplateEngine springTemplateEngine = new SpringTemplateEngine();
        springTemplateEngine.setTemplateResolver(templateResolver);

        MessageCountEmailGenerator snapshotGenerator = new MessageCountEmailGenerator(springTemplateEngine,
                unsubscribeTokenGenerator, emailProperties);

        MessageCountEmailContents contents = new MessageCountEmailContents();
        contents.setOrganizationName("Test Org");
        contents.setDeploymentTitle("Dev");
        contents.setStartDate(Instant.ofEpochMilli(1776902059000L));
        contents.setEndDate(Instant.ofEpochMilli(1776905659000L));
        contents.setMessageTypeList(List.of("BSM", "MAP", "TIM"));

        MessageCountCountsItem bsmCounts = new MessageCountCountsItem();
        bsmCounts.setIn(1000);
        bsmCounts.setOut(1000);
        bsmCounts.setDiffPercent(0.0);

        MessageCountCountsItem mapCounts = new MessageCountCountsItem();
        mapCounts.setIn(2000);
        mapCounts.setOut(1000);
        mapCounts.setDiffPercent(6.0);

        MessageCountCountsItem timCounts = new MessageCountCountsItem();
        timCounts.setIn(2000);
        timCounts.setOut(1000);
        timCounts.setDiffPercent(3.0);

        Map<String, MessageCountCountsItem> countsByType = Map.of(
                "BSM", bsmCounts,
                "MAP", mapCounts,
                "TIM", timCounts
        );

        MessageCountRsuItem rsuCountsItem = new MessageCountRsuItem();
        rsuCountsItem.setRsuIp("0.0.0.1");
        rsuCountsItem.setPrimaryRoute("route");
        rsuCountsItem.setMessageCountsByType(countsByType);

        contents.setRsuCounts(List.of(rsuCountsItem));

        EmailContent result = snapshotGenerator.generateEmailBody(contents);

        String snapshotPath = "emails/message_count_snapshot.html";
        SnapshotTestUtils.assertMatchesSnapshot(result.getBody(), snapshotPath);
    }

    @Test
    void generateEmailBody_mockedTest() {
        messageCountEmailGenerator = new MessageCountEmailGenerator(
                templateEngine,
                unsubscribeTokenGenerator,
                emailProperties);
        messageCountEmailGenerator = spy(messageCountEmailGenerator);

        

        MessageCountEmailContents contents = new MessageCountEmailContents();
        contents.setOrganizationName("Test Org");
        contents.setDeploymentTitle("Dev");
        contents.setStartDate(Instant.ofEpochMilli(1776902059000L));
        contents.setEndDate(Instant.ofEpochMilli(1776905659000L));
        contents.setMessageTypeList(List.of("BSM", "MAP", "TIM"));

        MessageCountCountsItem bsmCounts = new MessageCountCountsItem();
        bsmCounts.setIn(1000);
        bsmCounts.setOut(1000);
        bsmCounts.setDiffPercent(0.0);

        MessageCountCountsItem mapCounts = new MessageCountCountsItem();
        mapCounts.setIn(2000);
        mapCounts.setOut(1000);
        mapCounts.setDiffPercent(6.0);

        MessageCountCountsItem timCounts = new MessageCountCountsItem();
        timCounts.setIn(2000);
        timCounts.setOut(1000);
        timCounts.setDiffPercent(3.0);

        Map<String, MessageCountCountsItem> countsByType = Map.of(
                "BSM", bsmCounts,
                "MAP", mapCounts,
                "TIM", timCounts
        );

        MessageCountRsuItem rsuCountsItem = new MessageCountRsuItem();
        rsuCountsItem.setRsuIp("0.0.0.1");
        rsuCountsItem.setPrimaryRoute("route");
        rsuCountsItem.setMessageCountsByType(countsByType);

        contents.setRsuCounts(List.of(rsuCountsItem));

        Context thymeLeafContext = mock(Context.class);

        doCallRealMethod().when(messageCountEmailGenerator).generateEmailBody(any());

        when(messageCountEmailGenerator.generateEmailContextBasic()).thenReturn(thymeLeafContext);
        doNothing().when(thymeLeafContext).setVariable(anyString(), any());

        when(templateEngine.process("emails/email_template_message_counts", thymeLeafContext)).thenReturn("HTML CONTENT");

        EmailContent result = messageCountEmailGenerator.generateEmailBody(contents);

        EmailContent expectedResult = new EmailContent("CDOT-CV Dev ODE Counts", "HTML CONTENT");
        assertEquals(expectedResult, result);

        verify(thymeLeafContext, times(8)).setVariable(anyString(), any());
        verify(thymeLeafContext).setVariable("preview_text", "Message Counts from CV Manager");
        verify(thymeLeafContext).setVariable("footer_address", "CV-Manager Message Counts");
        verify(thymeLeafContext).setVariable("organizationName", "Test Org");
        verify(thymeLeafContext).setVariable("deploymentTitle", "Dev");
        verify(thymeLeafContext).setVariable("startDate", "2026-04-22T23:54:19Z");
        verify(thymeLeafContext).setVariable("endDate", "2026-04-23T00:54:19Z");
        verify(thymeLeafContext).setVariable("messageTypes", List.of("BSM", "MAP", "TIM"));
        verify(thymeLeafContext).setVariable("messageCounts", List.of(rsuCountsItem));
        verify(templateEngine).process("emails/email_template_message_counts", thymeLeafContext);
    }
}