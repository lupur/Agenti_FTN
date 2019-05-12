package com.example;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;

@Singleton
@Startup
public class CounterBean implements ICounter {

	private int counter; 
	
	@PostConstruct
	public void Init() {
		setCount(10);
	}

	public void increment() {
		counter++;
	}
	
	public int getCount() {
		return counter;
	}

	public void setCount(int counter) {
		this.counter = counter;
	}	
}
