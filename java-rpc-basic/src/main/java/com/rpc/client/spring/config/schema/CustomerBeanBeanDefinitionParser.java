package com.rpc.client.spring.config.schema;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import com.rpc.client.FactoryObjectUtil;

public class CustomerBeanBeanDefinitionParser implements BeanDefinitionParser {

	@Override
	public BeanDefinition parse(Element element, ParserContext parserContext) {
		GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
		try {
			String serviceId = element.getAttribute("id");
			String interfaceName = element.getAttribute("class");
			beanDefinition.setBeanClass(FactoryObjectUtil.class);
			beanDefinition.setAttribute("serviceId", serviceId);
			beanDefinition.setAttribute("serviceClass", Class.forName(interfaceName));
			MutablePropertyValues propertyValues = new MutablePropertyValues();
			propertyValues.add("serviceId", serviceId);
			propertyValues.add("serviceClass", Class.forName(interfaceName));
			beanDefinition.setPropertyValues(propertyValues);
			parserContext.getRegistry().registerBeanDefinition(serviceId, beanDefinition);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return beanDefinition;
	}

}
