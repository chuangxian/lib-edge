package myzd.services.impl;

import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class CheckContentType {

	public boolean checkRequestContentType(HttpServletRequest request){
		if(request.getMethod().equals("POST") ||
						request.getMethod().equals("PUT") ||
						request.getMethod().equals("PATCH")){
			if(request.getHeader("Content-Type").equals("application/json")){
				return true;
			}
			return false;
		}
		return true;
	}
}
