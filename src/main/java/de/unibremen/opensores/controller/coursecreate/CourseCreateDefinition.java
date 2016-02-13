package de.unibremen.opensores.controller.coursecreate;

import javax.enterprise.inject.Produces;
import javax.faces.flow.Flow;
import javax.faces.flow.builder.FlowBuilder;
import javax.faces.flow.builder.FlowBuilderParameter;
import javax.faces.flow.builder.FlowDefinition;
import java.io.Serializable;

/**
 * Java configuration for the courseCreate-Flow. Needed by JSF.
 * @author Stefan Oberdörfer
 */
public class CourseCreateDefinition implements Serializable {

    private static final long serialVersionUID = 4200648118969169611L;

    /**
     * Provides the flow for creating a new course to JSF.
     * @param flowBuilder Parameter used by JSF
     * @return Flow object for JSF to inject
     */
    @Produces
    @FlowDefinition
    public Flow defineCourseCreateFlow( @FlowBuilderParameter FlowBuilder flowBuilder) {

        String flowId = "coursecreate";   // id for this flow
        flowBuilder.id("", flowId); // set flow id

        // add a view to the flow and mark it as start node of the flow graph
        flowBuilder.viewNode("coursecreate", "/course/create/step1.xhtml").markAsStartNode();

        // add views to the flow
        flowBuilder.viewNode("step2", "/course/create/step2.xhtml");
        flowBuilder.viewNode("step3", "/course/create/step3.xhtml");
        flowBuilder.viewNode("step4", "/course/create/step4.xhtml");
        flowBuilder.viewNode("step5", "/course/create/step5.xhtml");

        // add a return node. The flow is exited with the outcome "course/overview"
        // once this node is reached.
        flowBuilder.returnNode("return-node")
                .fromOutcome("/course/overview.xhtml");

        // call this when the flow is entered
        flowBuilder.initializer("#{courseCreateFlowController.initialize()}");

        // call this when the flow is exited
        flowBuilder.finalizer("#{courseCreateFlowController.finalizeFlow()}");

        return flowBuilder.getFlow();
    }
}
