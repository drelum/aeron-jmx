package com.aeron.aeronjmx;

import java.lang.management.ManagementFactory;
import java.util.List;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class AeronMBean implements DynamicMBean {
	
    private static final Logger LOG = LogManager.getLogger(AeronMBean.class);


	JMXCountersReader countersReader = new JMXCountersReader();
	CounterInfo counterInfo;

	public AeronMBean(CounterInfo counterInfo) {
		this.counterInfo = counterInfo;
	}

	public Object getAttribute(String attribute) {	
		
		LOG.debug(attribute);
		
		List<CounterInfo> counters = countersReader.read();
		
		for (CounterInfo auxCounterInfo : counters) {
			if (auxCounterInfo.getName().equals(counterInfo.getName()))
				return auxCounterInfo.getValue();
		}	
		
		return null;
	}

	public void setAttribute(Attribute attribute)
			throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException {
		// TODO Auto-generated method stub
	}

	public AttributeList getAttributes(String[] attributes) {
		
		LOG.debug(attributes);
		AttributeList list = new AttributeList();

		for (String attribute : attributes) {
			
			Object val = getAttribute(attribute);
			if (val != null)				
				list.add(new Attribute(attribute, val));
		}	
		
		return list;
	}

	public AttributeList setAttributes(AttributeList attributes) {
		// TODO Auto-generated method stub
		return null;
	}

	public Object invoke(String actionName, Object[] params, String[] signature)
			throws MBeanException, ReflectionException {
		// TODO Auto-generated method stub
		return null;
	}

	public MBeanInfo getMBeanInfo() {
		return counterInfo.getMBeanInfo();
	}

	public static void startMBeanServer(String aeronCncFileName) {
		
		LOG.info("Starting " + AeronMBean.class.getSimpleName());

		MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();

		final JMXCountersReader countersReader;
		
		if (aeronCncFileName != null)
			countersReader = new JMXCountersReader(aeronCncFileName);
		else 
			countersReader = new JMXCountersReader();
		
		try {			
			
			new Thread(() -> {
				
				while(true) {
					
					
					List<CounterInfo> list = countersReader.read();
					
					for (CounterInfo counterInfo : list) {
						AeronMBean dynamicMBean = new AeronMBean(counterInfo);

						//System.out.println(counterInfo.getName());
						try {
							ObjectName objectName = new ObjectName(counterInfo.getName());
							if (!mbeanServer.isRegistered(objectName)) {
								LOG.info("Registering " + objectName);
								mbeanServer.registerMBean(dynamicMBean, objectName);
							}
							
						} catch (InstanceAlreadyExistsException | MBeanRegistrationException
								| NotCompliantMBeanException | MalformedObjectNameException e) {
							e.printStackTrace();
						}
					}
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}).start();		

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		try {
			if (args.length > 1)
				showUsage();		
			else if (args.length == 1)
				startMBeanServer(args[0]);
			else 
				startMBeanServer(null);
		} catch (Exception e) {
			e.printStackTrace();
			showUsage();
		}
	}
	
	private static void showUsage() {
		System.out.println("optional parameter pointing to AeronCncFile. If no parameter is provided the default cnc file for current user will be used");
	}

}
