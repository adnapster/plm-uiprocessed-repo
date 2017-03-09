package com.uiprocesser.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.uiprocesser.dao.PLMUiprocessDao;

@Service
public class PLMUiprocessServiceImpl implements PLMUiprocessService {
	
	@Autowired 
	PLMUiprocessDao uiProcessDao;

	@Override
	public String readBlobXML(String ecnNumber) {
		// TODO Auto-generated method stub
		return uiProcessDao.readBlobXML(ecnNumber);
	}

}
