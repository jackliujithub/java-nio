package com.rpc.client.spring.config.schema;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

public class RpcNamespaceHandler extends NamespaceHandlerSupport{

	@Override
	public void init() {
		registerBeanDefinitionParser("bean", new CustomerBeanBeanDefinitionParser());
	}

}
