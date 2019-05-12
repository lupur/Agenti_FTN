package com.example;

import javax.ejb.Local;

@Local
public interface ICounter {

	void increment();
	int getCount();
}
