package io.iktech.jenkins.plugins.artifactor;

import hudson.model.TaskListener;
import io.artifactz.client.Feedback;
import io.artifactz.client.FeedbackLevel;

import java.io.PrintStream;

public class FeedbackImpl implements Feedback {
    private final PrintStream logger;

    public FeedbackImpl(TaskListener taskListener) {
        this.logger = taskListener != null ? taskListener.getLogger() : System.out;
    }

    @Override
    public void send(FeedbackLevel feedbackLevel, String s) {
        this.logger.println(s);
    }
}
