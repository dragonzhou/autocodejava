package com.auto;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;

/**
 * @author haibo.huang
 *
 */
public class BeanFactoryUtil implements BeanFactoryAware {

	private static BeanFactory beanFactory;
	
	@SuppressWarnings("static-access")
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
	}
	
	public static Object getBean(String beanName){
		return beanFactory.getBean(beanName);
		
	}
	
	public static boolean containsBean(String beanName){
		return beanFactory.containsBean(beanName);
	}

}
