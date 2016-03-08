package service;

import model.DataProc;

import org.springframework.web.bind.support.SessionStatus;

import domain.Myrequest;




public interface MyRequestService {


	void create( final DataProc createProc, final Myrequest pgenRequest, SessionStatus status);
}
