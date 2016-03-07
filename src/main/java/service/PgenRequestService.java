package service;

import java.security.Principal;

import org.springframework.web.bind.support.SessionStatus;

import com.total.twister.common.domain.DataProc;
import com.total.twister.common.domain.Operator;
import com.total.twister.pgen.domain.PgenRequest;




public interface PgenRequestService {


	void create(Operator activeUser, final DataProc createProc, final PgenRequest pgenRequest, SessionStatus status);
	void correct(Principal loggedUser, final DataProc correctProc, final PgenRequest pgenRequest);
	void execute( final DataProc executeProc, final PgenRequest pgenRequest, Operator activeUser);
	void complete( final DataProc executeProc, final PgenRequest pgenRequest, Operator activeUser);
	void moreInfo(final DataProc moreInfoProc, final PgenRequest pgenRequest, Operator activeUser);
	void close( final DataProc closeProc, final PgenRequest pgenRequest, Operator activeUser);
	void reject( final DataProc rejectProc, final PgenRequest pgenRequest, Operator activeUser);
	void abandon(final DataProc abandonProc, final PgenRequest pgenRequest, Operator activeUser);
	void control(final DataProc controlProc,final PgenRequest pgenRequest, Operator activeUser);
	void validate(final DataProc validateProc, final PgenRequest pgenRequest, Operator activeUser);

	void auto( Long dataId,final DataProc itsmProc,  final PgenRequest pgenRequest, Operator activeUser);

}
