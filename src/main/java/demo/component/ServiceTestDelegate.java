package demo.component;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by daniel on 06.07.16.
 */
@Component
public class ServiceTestDelegate implements JavaDelegate {

    @Autowired
    private DiscoveryClient discoveryClient;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        List<ServiceInstance> instances = this.discoveryClient.getInstances("a-bootiful-client");
        ServiceInstance instance = instances.get(0);
        List<String> services = this.discoveryClient.getServices();
        System.out.println("instances: " + instances);
        services.forEach(s -> System.out.println("service name: " + s));
    }

}
