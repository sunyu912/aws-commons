package io.magnum.awscommons;

import java.util.List;

import com.amazonaws.services.autoscaling.model.AutoScalingGroup;
import com.amazonaws.services.cloudformation.model.Output;
import com.amazonaws.services.cloudformation.model.Parameter;
import com.amazonaws.services.cloudformation.model.Stack;
import com.amazonaws.services.cloudformation.model.StackResource;
import com.amazonaws.services.elasticloadbalancing.model.LoadBalancerDescription;

/**
 * A convenience class that encapsulates a typical AWS stack we use.
 * 
 * @author Yu Sun
 */
public class HD4ARStack implements Comparable<HD4ARStack> {
    
    /**
     * Well-known parameters for our stacks.
     */
    public static enum InputAttribute {
        /** Key for the ID of the stack's AMI */
        IMAGE_ID("ImageId"),

        /** Key for the stack's availability zone */
        AVAILABILITY_ZONE("Zone"),
        
        /** Key for the stack's size */
        SIZE("Size");

        private final String key;
        
        private InputAttribute(String key) {
            this.key = key;
        }

        public String getKey() {
            return key;
        }
        
        String fetchValue(List<Parameter> parameters) {
            for(Parameter param : parameters) {
                if(key.equals(param.getParameterKey())) {
                    return param.getParameterValue();
                }
            }
            return null;
        }
    }
    
    /**
     * Well-known outputs for our stacks.
     */
    public enum OutputAttribute {
        /**
         * Key for the stack's resource notification queue.
         */
        RESOURCE_NOTIFICATION_QUEUE_URL("resourceNotificationQueueUrl"), 
        
        /**
         * Key for the stack's cache ID. 
         */
        RESOURCE_CACHE_CLUSTER_ID("resourceCache");
        
        private final String key;
        
        private OutputAttribute(String key) {
            this.key = key;
        }

        public String getKey() {
            return key;
        }

        String fetchValue(List<Output> outputs) {
            for(Output output : outputs) {
                if(key.equals(output.getOutputKey())) {
                    return output.getOutputValue();
                }
            }
            return null;
        }
    }
    
    private final Stack cloudFormationStack;
    private final String template;
    private final List<StackResource> stackResources;
    private final AutoScalingGroup autoScalingGroup;    
    private final LoadBalancerDescription loadBalancer;    

    HD4ARStack(Stack cloudFormationStack, String template, List<StackResource> stackResources,
            AutoScalingGroup autoScalingGroup, LoadBalancerDescription loadBalancer) {
        if (cloudFormationStack == null) throw new IllegalArgumentException("missing cloudFormationStack");
        if (template == null) throw new IllegalArgumentException("missing template");
        if (stackResources == null) throw new IllegalArgumentException("missing stackResources");

        this.cloudFormationStack = cloudFormationStack;
        this.template = template;
        this.stackResources = stackResources;
        this.autoScalingGroup = autoScalingGroup;
        this.loadBalancer = loadBalancer;
    }

    public Stack getCloudFormationStack() {
        return cloudFormationStack;
    }

    public String getTemplate() {
        return template;
    }
    
    public List<StackResource> getStackResources() {
        return stackResources;
    }

    public AutoScalingGroup getAutoScalingGroup() {
        return autoScalingGroup;
    }
    
    public LoadBalancerDescription getLoadBalancer() {
        return loadBalancer;
    }

    public String getAttribute(OutputAttribute attribute) {
        return attribute.fetchValue(getCloudFormationStack().getOutputs());
    }

    public String getAttribute(InputAttribute attribute) {
        return attribute.fetchValue(getCloudFormationStack().getParameters());
    }
    
    @Override
    public int compareTo(HD4ARStack o) {
        return getCloudFormationStack().getStackName().compareTo(o.getCloudFormationStack().getStackName());
    }

    public StringBuilder prettyPrint(StringBuilder buffer) {
        if (buffer == null) {
            buffer = new StringBuilder();
        }
        String zone = getAttribute(InputAttribute.AVAILABILITY_ZONE);
        String imageId = getAttribute(InputAttribute.IMAGE_ID);
        buffer.append("Stack '" + getCloudFormationStack().getStackName() + "' @ " + zone + ":\n");
        buffer.append("    AMI : " + imageId + "\n");
        
        String elbStr;
        if(getLoadBalancer() != null) {
            elbStr = getLoadBalancer().getLoadBalancerName() + " (" + getLoadBalancer().getInstances().size() + " instances)";
        } else {
            elbStr = "none";
        }
        buffer.append("    Load Balancer : " + elbStr + "\n");
        
        String asgStr;
        if(getAutoScalingGroup() != null) {
            asgStr = getAutoScalingGroup().getAutoScalingGroupName() + " (" + getAutoScalingGroup().getInstances().size() + " instances)";
        } else {
            asgStr = "none";
        }
        buffer.append("    Auto Scaling Group : " + asgStr + "\n");
        return buffer;
    }

    public String prettyPrint() {
        return prettyPrint(null).toString();
    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName() + " ["
                + "cloudFormationStack=" + getCloudFormationStack()
                + ", stackName=" + getCloudFormationStack().getStackName()
                + ", stackId=" + getCloudFormationStack().getStackId()
                + ", stackResources=" + getStackResources()
                + ", imageId=" + getAttribute(InputAttribute.IMAGE_ID)
                + ", availabilityZone=" + getAttribute(InputAttribute.AVAILABILITY_ZONE)
                + ", loadBalancer=" + getLoadBalancer()
                + ", autoScalingGroup=" + getAutoScalingGroup()
                + "]";
    } 
}
