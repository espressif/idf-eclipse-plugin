package com.espressif.idf.core.component.registry;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.espressif.idf.core.component.vo.ComponentVO;


/**
 * Singleton class to manage components that are available to be added.
 * @author Ali Azam Rana <ali.azamrana@espressif.com>
 *
 */
public class ComponentToAdd
{
	private static ComponentToAdd INSTANCE;
	private Queue<ComponentVO> components;	
	
	private ComponentToAdd()
	{
		components = new ConcurrentLinkedQueue<>();
	}
	
	public static ComponentToAdd getInstance()
	{
		if (INSTANCE == null)
		{
			synchronized (ComponentToAdd.class)
			{
				if (INSTANCE == null)
				{
					INSTANCE = new ComponentToAdd();
				}
			}
		}
		return INSTANCE;
	}
	
	
	public boolean isComponentAvailableToAdd()
	{
		return !getInstance().components.isEmpty();
	}
	
	public ComponentVO getComponentToAdd()
	{
		return getInstance().components.poll();
	}
	
	public void addComponent(ComponentVO component)
	{
		getInstance().components.add(component);
	}
}
