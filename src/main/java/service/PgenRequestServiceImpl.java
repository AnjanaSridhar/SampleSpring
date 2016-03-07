package service;

import java.security.Principal;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.support.SessionStatus;

import com.total.twister.bpm.BPMService;
import com.total.twister.common.domain.DataProc;
import com.total.twister.common.domain.DataProcComment;
import com.total.twister.common.domain.DataProcFile;
import com.total.twister.common.domain.DataProcHistory;
import com.total.twister.common.domain.LocalizedMessage;
import com.total.twister.common.domain.Operator;
import com.total.twister.common.domain.ProcessTaskVersion;
import com.total.twister.common.reference.TaskRefConstants;
import com.total.twister.common.reference.TwisterConstants;
import com.total.twister.common.reference.WorkflowConstants;
import com.total.twister.infra.exception.TwisterGenericRuntimeException;
import com.total.twister.pgen.domain.PgenRequest;
import com.total.twister.pgen.reference.PgenConstants;
import com.total.twister.web.pgen.PgenRequestController;
import com.total.twister.web.reference.ITwisterStatus;
import com.total.twister.web.utils.GenericUtils;


@Service@Transactional(isolation = Isolation.DEFAULT, propagation=Propagation.REQUIRED)
public class PgenRequestServiceImpl extends PgenServiceImpl implements PgenRequestService{

	private Logger log = LoggerFactory.getLogger(PgenRequestServiceImpl.class);

	private Logger logSecurity = LoggerFactory.getLogger("TwisterSec");



	/**
	 * definePgenStatus function is called to Define the Pgen request Status
	 * 
	 * @param dataProc - DataProc
	 * @param pgenRequest - PgenRequest
	 */
	private void definePgenStatus(DataProc dataProc, PgenRequest pgenRequest) {

		if(pgenRequest.getRequestType().equals(PgenConstants.PGEN_CREATE_BANK_COUNTERPARTY)){
			dataProc.setStatus(localizedMessageService.findLocalizedMessage(PgenConstants.PGEN_PREFIX+TwisterConstants.STATUS_TO_COMPLETE));
		}
		else if(pgenRequest.getRequestType().equals(PgenConstants.PGEN_CREATE_THIRD) || pgenRequest.getRequestType().equals(PgenConstants.PGEN_MODIFY_THIRD) || pgenRequest.getRequestType().equals(PgenConstants.PGEN_MODIFY_BANK_COUNTERPARTY) || pgenRequest.getRequestType().equals(PgenConstants.PGEN_MODIFY_SWIFT_MESSAGE)){
			dataProc.setStatus(localizedMessageService.findLocalizedMessage(PgenConstants.PGEN_PREFIX+TwisterConstants.STATUS_TO_VALIDATE));   
		}
		else{
			dataProc.setStatus(localizedMessageService.findLocalizedMessage(PgenConstants.PGEN_PREFIX+TwisterConstants.STATUS_TO_EXECUTE));
		}

	}



	public void create(Operator activeUser, final DataProc createProc, final PgenRequest pgenRequest, SessionStatus status){
		LocalizedMessage tempStatus;
		String code = "";
		int version = 0;
		Operator last_oper = null;
		Set<DataProcComment> commentsSet = new HashSet<DataProcComment>();
		Set<DataProcFile> filesSet = new HashSet<DataProcFile>();
		ProcessTaskVersion task_code = null;
		
		DataProc dataProc = null;
		BPMService bonitaService = new BPMService();

		if(createProc.getId()!=null){
			 tempStatus = createProc.getStatus();
			 code = tempStatus.getCode();
			 version = createProc.getVersion();
			 last_oper = createProc.getOperLast();
			 commentsSet = createProc.getComments();
			 filesSet = createProc.getFiles();
			 task_code = createProc.getProcessTaskVersion();
			dataProc = dataProcService.findDataProc(createProc.getId());
		}
		else{
			dataProc = createProc;
		}
		Boolean internFlag = GenericUtils.containsProfileByCode(activeUser.getProfiles(),PgenConstants.PGEN_INTERN_PROFILE);

		createIt(internFlag,dataProc, bonitaService, activeUser, createProc, pgenRequest, status);

		processForm(bonitaService, dataProc, pgenRequest, PgenConstants.PGEN_ACTION_CREATE,WorkflowConstants.ACTION_TYPE_TO_VALIDATE,WorkflowConstants.PROCESS_PGEN, internFlag);
		boolean isCreate = ( createProc.getIdTaskInstance()==null) ? true:false ;
		if(PgenRequestController.bonitaStatus.equals("failure")){
			if(task_code!=null){
				dataProc.setProcessTaskVersion(task_code);
			}
			if(isCreate==false){
				rollbackDataProcChanges(dataProc, commentsSet, filesSet, version, last_oper, code, pgenRequest);
			}
			
			
		}
		else{
			dataProcService.refreshDataProc(dataProc);
		}

	}


	public void correct(Principal loggedUser, final DataProc correctProc, final PgenRequest pgenRequest){
		
		LocalizedMessage tempStatus = correctProc.getStatus();
		String code = tempStatus.getCode();
		int version = correctProc.getVersion();
		Operator last_oper = correctProc.getOperLast();
		Set<DataProcComment> commentsSet = correctProc.getComments();
		Set<DataProcFile> filesSet = correctProc.getFiles();
		ProcessTaskVersion task_code = correctProc.getProcessTaskVersion();
		
		DataProc dataProc = dataProcService.findDataProc(correctProc.getId());
		Operator activeUser = operatorService.findOperatorByLogin(loggedUser.getName());
		//If the user is a flag, send the info to bonita
		Boolean internFlag = GenericUtils.containsProfileByCode(activeUser.getProfiles(),PgenConstants.PGEN_INTERN_PROFILE);
		BPMService bonitaService = new BPMService();
		
		correctIt(dataProc,loggedUser, correctProc, pgenRequest);

		processForm(bonitaService, dataProc, pgenRequest, PgenConstants.PGEN_ACTION_CREATE,WorkflowConstants.ACTION_TYPE_TO_EXECUTE,WorkflowConstants.PROCESS_PGEN, internFlag);
		
		if(PgenRequestController.bonitaStatus.equals("failure")){
			dataProc.setProcessTaskVersion(task_code);
			rollbackDataProcChanges(dataProc, commentsSet, filesSet, version, last_oper, code, pgenRequest);
		}
		else{
			dataProcService.refreshDataProc(dataProc);
		}

	}


	public void execute( final DataProc executeProc, final PgenRequest pgenRequest, Operator activeUser){

		LocalizedMessage tempStatus = executeProc.getStatus();
		String code = tempStatus.getCode();
		int version = executeProc.getVersion();
		Operator last_oper = executeProc.getOperLast();
		Set<DataProcComment> commentsSet = executeProc.getComments();
		Set<DataProcFile> filesSet = executeProc.getFiles();
		ProcessTaskVersion task_code = executeProc.getProcessTaskVersion();
		
		DataProc dataProc = dataProcService.findDataProc(executeProc.getId());

		//If the user is a flag, send the info to bonita
		Boolean internFlag = GenericUtils.containsProfileByCode(activeUser.getProfiles(),PgenConstants.PGEN_INTERN_PROFILE);
		BPMService bonitaService = new BPMService();
		executeIt(dataProc,executeProc, pgenRequest, activeUser);

		processForm(bonitaService, dataProc, pgenRequest, PgenConstants.PGEN_ACTION_CREATE,WorkflowConstants.ACTION_TYPE_TO_EXECUTE,WorkflowConstants.PROCESS_PGEN, internFlag);
		if(PgenRequestController.bonitaStatus.equals("failure")){
			dataProc.setProcessTaskVersion(task_code);
			rollbackDataProcChanges(dataProc, commentsSet, filesSet, version, last_oper, code, pgenRequest);
		}
		else{
			dataProcService.refreshDataProc(dataProc);
		}
		

	}



	public void complete( final DataProc executeProc, final PgenRequest pgenRequest, Operator activeUser){

		LocalizedMessage tempStatus = executeProc.getStatus();
		String code = tempStatus.getCode();
		int version = executeProc.getVersion();
		Operator last_oper = executeProc.getOperLast();
		Set<DataProcComment> commentsSet = executeProc.getComments();
		Set<DataProcFile> filesSet = executeProc.getFiles();
		ProcessTaskVersion task_code = executeProc.getProcessTaskVersion();
		
		DataProc dataProc = dataProcService.findDataProc(executeProc.getId());

		//If the user is a flag, send the info to bonita
		Boolean internFlag = GenericUtils.containsProfileByCode(activeUser.getProfiles(),PgenConstants.PGEN_INTERN_PROFILE);
		BPMService bonitaService = new BPMService();

		completeIt(dataProc,executeProc, pgenRequest, activeUser);

		processForm(bonitaService, dataProc, pgenRequest, PgenConstants.PGEN_ACTION_CREATE,WorkflowConstants.ACTION_TYPE_TO_VALIDATE,WorkflowConstants.PROCESS_PGEN, internFlag);
		if(PgenRequestController.bonitaStatus.equals("failure")){
			dataProc.setProcessTaskVersion(task_code);
			rollbackDataProcChanges(dataProc, commentsSet, filesSet, version, last_oper, code, pgenRequest);
		}
		else{
			dataProcService.refreshDataProc(dataProc);
		}

	}




	public void moreInfo(final DataProc moreInfoProc, final PgenRequest pgenRequest, Operator activeUser){

		LocalizedMessage tempStatus = moreInfoProc.getStatus();
		String code = tempStatus.getCode();
		int version = moreInfoProc.getVersion();
		Operator last_oper = moreInfoProc.getOperLast();
		Set<DataProcComment> commentsSet = moreInfoProc.getComments();
		Set<DataProcFile> filesSet = moreInfoProc.getFiles();
		ProcessTaskVersion task_code = moreInfoProc.getProcessTaskVersion();
		
		DataProc dataProc = dataProcService.findDataProc(moreInfoProc.getId());
		
		//If the user is a flag, send the info to bonita
		Boolean internFlag = GenericUtils.containsProfileByCode(activeUser.getProfiles(),PgenConstants.PGEN_INTERN_PROFILE);

		BPMService bonitaService = new BPMService();
		getMoreInfo(dataProc, moreInfoProc, pgenRequest, activeUser);

		processForm(bonitaService, dataProc, pgenRequest, PgenConstants.PGEN_ACTION_CREATE,WorkflowConstants.ACTION_TYPE_TO_MODIFY,WorkflowConstants.PROCESS_PGEN, internFlag);
		if(PgenRequestController.bonitaStatus.equals("failure")){
			dataProc.setProcessTaskVersion(task_code);
			rollbackDataProcChanges(dataProc, commentsSet, filesSet, version, last_oper, code, pgenRequest);
		}
		else{
			dataProcService.refreshDataProc(dataProc);
		}
	}



	public void close( final DataProc closeProc, final PgenRequest pgenRequest, Operator activeUser){

		LocalizedMessage tempStatus = closeProc.getStatus();
		String code = tempStatus.getCode();
		int version = closeProc.getVersion();
		Operator last_oper = closeProc.getOperLast();
		Set<DataProcComment> commentsSet = closeProc.getComments();
		Set<DataProcFile> filesSet = closeProc.getFiles();	
		ProcessTaskVersion task_code = closeProc.getProcessTaskVersion();
		
		DataProc dataProc = dataProcService.findDataProc(closeProc.getId());
		
		//If the user is a flag, send the info to bonita
		Boolean internFlag = GenericUtils.containsProfileByCode(activeUser.getProfiles(),PgenConstants.PGEN_INTERN_PROFILE);

		BPMService bonitaService = new BPMService();

		closeIt(dataProc, closeProc, pgenRequest, activeUser);

		processForm(bonitaService, dataProc, pgenRequest, PgenConstants.PGEN_ACTION_ABONDAN,WorkflowConstants.ACTION_TYPE_TO_CLOSE,WorkflowConstants.PROCESS_PGEN, internFlag);
		if(PgenRequestController.bonitaStatus.equals("failure")){
			dataProc.setProcessTaskVersion(task_code);
			rollbackDataProcChanges(dataProc, commentsSet, filesSet, version, last_oper, code, pgenRequest);
		}
		else{
			dataProcService.refreshDataProc(dataProc);
		}
	}


	public void reject( final DataProc rejectProc, final PgenRequest pgenRequest, Operator activeUser){

		LocalizedMessage tempStatus = rejectProc.getStatus();
		String code = tempStatus.getCode();
		int version = rejectProc.getVersion();
		Operator last_oper = rejectProc.getOperLast();
		Set<DataProcComment> commentsSet = rejectProc.getComments();
		Set<DataProcFile> filesSet = rejectProc.getFiles();
		ProcessTaskVersion task_code = rejectProc.getProcessTaskVersion();
		
		DataProc dataProc = dataProcService.findDataProc(rejectProc.getId());

		//If the user is a flag, send the info to bonita
		Boolean internFlag = GenericUtils.containsProfileByCode(activeUser.getProfiles(),PgenConstants.PGEN_INTERN_PROFILE);

		BPMService bonitaService = new BPMService();

		rejectIt(dataProc, rejectProc, pgenRequest, activeUser);

		processForm(bonitaService, dataProc, pgenRequest, PgenConstants.PGEN_ACTION_ABONDAN,WorkflowConstants.ACTION_TYPE_TO_REJECT,WorkflowConstants.PROCESS_PGEN, internFlag);
		if(PgenRequestController.bonitaStatus.equals("failure")){
			dataProc.setProcessTaskVersion(task_code);
			rollbackDataProcChanges(dataProc, commentsSet, filesSet, version, last_oper, code, pgenRequest);
		}
		else{
			dataProcService.refreshDataProc(dataProc);
		}
	}



	public void abandon(final DataProc abandonProc, final PgenRequest pgenRequest, Operator activeUser){

		LocalizedMessage tempStatus = abandonProc.getStatus();
		String code = tempStatus.getCode();
		int version = abandonProc.getVersion();
		Operator last_oper = abandonProc.getOperLast();
		Set<DataProcComment> commentsSet = abandonProc.getComments();
		Set<DataProcFile> filesSet = abandonProc.getFiles();
		ProcessTaskVersion task_code = abandonProc.getProcessTaskVersion();
		
		DataProc dataProc = dataProcService.findDataProc(abandonProc.getId());

		//If the user is a flag, send the info to bonita
		Boolean internFlag = GenericUtils.containsProfileByCode(activeUser.getProfiles(),PgenConstants.PGEN_INTERN_PROFILE);
		BPMService bonitaService = new BPMService();

		abandonIt(dataProc, abandonProc, pgenRequest, activeUser);

		processForm(bonitaService, dataProc, pgenRequest, PgenConstants.PGEN_ACTION_ABONDAN,WorkflowConstants.ACTION_TYPE_TO_ABANDON,WorkflowConstants.PROCESS_PGEN, internFlag);
		if(PgenRequestController.bonitaStatus.equals("failure")){
			dataProc.setProcessTaskVersion(task_code);
			rollbackDataProcChanges(dataProc, commentsSet, filesSet, version, last_oper, code, pgenRequest);
		}
		else{
			dataProcService.refreshDataProc(dataProc);
		}

	}



	public void control(final DataProc controlProc,final PgenRequest pgenRequest, Operator activeUser){
		BPMService bonitaService = new BPMService();
		
		LocalizedMessage tempStatus = controlProc.getStatus();
		String code = tempStatus.getCode();
		int version = controlProc.getVersion();
		Operator last_oper = controlProc.getOperLast();
		Set<DataProcComment> commentsSet = controlProc.getComments();
		Set<DataProcFile> filesSet = controlProc.getFiles();		
		ProcessTaskVersion task_code = controlProc.getProcessTaskVersion();
		
		DataProc dataProc = dataProcService.findDataProc(controlProc.getId());
		
		//If the user is a flag, send the info to bonita
		Boolean internFlag = GenericUtils.containsProfileByCode(activeUser.getProfiles(),PgenConstants.PGEN_INTERN_PROFILE);

		controlIt(dataProc, controlProc, pgenRequest, activeUser);
		processForm(bonitaService, dataProc, pgenRequest, PgenConstants.PGEN_ACTION_ABONDAN,WorkflowConstants.ACTION_TYPE_TO_CONTROL,WorkflowConstants.PROCESS_PGEN, internFlag);
		//dataProcService.refreshDataProc(dataProc);
		if(PgenRequestController.bonitaStatus.equals("failure")){
			dataProc.setProcessTaskVersion(task_code);
			rollbackDataProcChanges(dataProc, commentsSet, filesSet, version, last_oper, code, pgenRequest);
		}
		else{
			dataProcService.refreshDataProc(dataProc);
		}
	}


	private void rollbackDataProcChanges(DataProc dataProc, Set<DataProcComment> commentsSet, Set<DataProcFile> filesSet, int version, Operator last_oper, String code,	PgenRequest pgenRequest){
		Set<DataProcComment> dataProcCommentsSet = dataProc.getComments();
		Set<DataProcFile> dataProcFilesSet = dataProc.getFiles();
		if(dataProcCommentsSet.size()>commentsSet.size()){
			 List<DataProcComment> comments = dataProcCommentService.findAllDataProcCommentsForProcInternalId(dataProc);
			 dataProcCommentService.deleteDataProcComment(comments.get(0));
		 }
		 
		 if(dataProcFilesSet.size()>filesSet.size()){
			 List<DataProcFile> files = dataProcFileService.findAllDataProcFilesByRequestIdDesc(dataProc.getId().toString());
			 dataProcFileService.deleteDataProcFile(files.get(files.size()-1));
		 }
		 
		 dataProc.setStatus(localizedMessageService.findLocalizedMessage(code));
		 dataProc.setVersion(version);
		 dataProc.setOperLast(last_oper);
		 persistDataProc(dataProc, pgenRequest);
	}

	public void validate(final DataProc validateProc, final PgenRequest pgenRequest, Operator activeUser ){
		LocalizedMessage tempStatus = validateProc.getStatus();
		String code = tempStatus.getCode();
		int version = validateProc.getVersion();
		Operator last_oper = validateProc.getOperLast();
		Set<DataProcComment> commentsSet = validateProc.getComments();
		Set<DataProcFile> filesSet = validateProc.getFiles();	
		ProcessTaskVersion task_code = validateProc.getProcessTaskVersion();
		
		DataProc dataProc = dataProcService.findDataProc(validateProc.getId());
		BPMService bonitaService = new BPMService();
		validateIt(dataProc, validateProc, pgenRequest, activeUser);
		
		Boolean internFlag = GenericUtils.containsProfileByCode(activeUser.getProfiles(),PgenConstants.PGEN_INTERN_PROFILE);

		processForm(bonitaService, dataProc, pgenRequest, PgenConstants.PGEN_ACTION_ABONDAN,WorkflowConstants.ACTION_TYPE_TO_VALIDATE,WorkflowConstants.PROCESS_PGEN, internFlag);
		if(PgenRequestController.bonitaStatus.equals("failure")){
			dataProc.setProcessTaskVersion(task_code);
			rollbackDataProcChanges(dataProc, commentsSet, filesSet, version, last_oper, code, pgenRequest);
		}
		else{
			dataProcService.refreshDataProc(dataProc);
		}

	}


	
	private void createIt(Boolean internFlag, DataProc dataProc, BPMService bonitaService, Operator activeUser, final DataProc createProc, final PgenRequest pgenRequest, SessionStatus status){


		//If the user is a flag, send the info to bonita

		//Override the PtV with the one selected in the list of the request.
		dataProc.setProcessTaskVersion(processTaskVersionService.findActiveProcessTaskVersion(pgenRequest.getRequestType(), TaskRefConstants.CODE_CREATION));
		dataProc.setProcData(serializeProcData(pgenRequest, PgenRequest.class));
		if(internFlag){
			dataProc.setStatus(localizedMessageService.findLocalizedMessage(PgenConstants.PGEN_PREFIX+TwisterConstants.STATUS_TO_VALIDATE));
		}else{
			definePgenStatus(dataProc,pgenRequest);
		}
	}

	private void correctIt(DataProc dataProc, Principal loggedUser, final DataProc correctProc, final PgenRequest pgenRequest){

		//If the user is a flag, send the info to bonita

		dataProc.setProcData(serializeProcData(pgenRequest, PgenRequest.class));
		dataProc.setStatus(localizedMessageService.findLocalizedMessage(PgenConstants.PGEN_PREFIX+TwisterConstants.STATUS_TO_CORRECT));
	}

	private void executeIt( DataProc dataProc , final DataProc executeProc, final PgenRequest pgenRequest, Operator activeUser){


		dataProc.setProcData(serializeProcData(pgenRequest, PgenRequest.class));
		dataProc.setStatus(localizedMessageService.findLocalizedMessage(PgenConstants.PGEN_PREFIX+TwisterConstants.STATUS_TO_CONTROL));

	}


	private void completeIt(DataProc dataProc ,  final DataProc executeProc, final PgenRequest pgenRequest, Operator activeUser){

		dataProc.setProcData(serializeProcData(pgenRequest, PgenRequest.class));
		dataProc.setStatus(localizedMessageService.findLocalizedMessage(PgenConstants.PGEN_PREFIX+TwisterConstants.STATUS_TO_VALIDATE));

	}



	private void getMoreInfo(DataProc dataProc , final DataProc moreInfoProc, final PgenRequest pgenRequest, Operator activeUser){

		dataProc.setProcData(serializeProcData(pgenRequest, PgenRequest.class));
		dataProc.setStatus(localizedMessageService.findLocalizedMessage(PgenConstants.PGEN_PREFIX+TwisterConstants.STATUS_ASK_MODIFICATION));

	}


	private void closeIt( DataProc dataProc ,final DataProc closeProc, final PgenRequest pgenRequest, Operator activeUser){

		dataProc.setProcData(serializeProcData(pgenRequest, PgenRequest.class));
		dataProc.setStatus(localizedMessageService.findLocalizedMessage(TwisterConstants.STATUS_TO_CLOSE));

	}

	private void rejectIt( DataProc dataProc ,final DataProc rejectProc, final PgenRequest pgenRequest, Operator activeUser){

		dataProc.setProcData(serializeProcData(pgenRequest, PgenRequest.class));
		dataProc.setStatus(localizedMessageService.findLocalizedMessage(PgenConstants.PGEN_PREFIX+TwisterConstants.STATUS_TO_REJECT+"_VAL"));


	}


	private void abandonIt(DataProc dataProc ,final DataProc abandonProc, final PgenRequest pgenRequest, Operator activeUser){

		dataProc.setProcData(serializeProcData(pgenRequest, PgenRequest.class));
		dataProc.setStatus(localizedMessageService.findLocalizedMessage(TwisterConstants.STATUS_TO_ABANDON));

	}

	private void controlIt(DataProc dataProc, final DataProc controlProc,final PgenRequest pgenRequest, Operator activeUser){

		dataProc.setProcData(serializeProcData(pgenRequest, PgenRequest.class));
		dataProc.setStatus(localizedMessageService.findLocalizedMessage(PgenConstants.PGEN_PREFIX+TwisterConstants.STATUS_TO_CONTROL));

	}


	private void validateIt(DataProc dataProc, final DataProc validateProc, final PgenRequest pgenRequest, Operator activeUser ){

		dataProc.setProcData(serializeProcData(pgenRequest, PgenRequest.class));
		/*
		 * If an intern created a request and the manager validates his/her request, it doesn't necessarily go to CCSIT.
		 * => DefineStatus as if a regular profile did a creation (cf. create() method)
		 */
		if(dataProc.getTask().getCode().equals(TaskRefConstants.PGEN_VALIDATION_RESP)){
			//Same method called in creation
			definePgenStatus(dataProc, pgenRequest); 
		}
		else{
			dataProc.setStatus(localizedMessageService.findLocalizedMessage(PgenConstants.PGEN_PREFIX+TwisterConstants.STATUS_TO_EXECUTE));
		}
	
	}









	public void auto( Long dataId,final DataProc itsmProc,  final PgenRequest pgenRequest, Operator activeUser){
		LocalizedMessage tempStatus = itsmProc.getStatus();
		String code = "";
		if(tempStatus!=null){
			code = tempStatus.getCode();
		}
		int version = 0;
		if(itsmProc.getVersion()!=null){
			version = itsmProc.getVersion();
		}
		Operator last_oper = null;
		if(itsmProc.getOperLast()!=null){
			last_oper = itsmProc.getOperLast();
		}
		Set<DataProcComment> commentsSet = null;
		if(itsmProc.getComments()!=null){
			commentsSet = itsmProc.getComments();
		}
		Set<DataProcFile> filesSet = null;
		if(itsmProc.getFiles()!=null){
			filesSet = itsmProc.getFiles();
		}
		DataProc dataproc = dataProcService.findDataProc(dataId);
		if(dataproc.getItsmNumber()==null){
			Map<String,Object> workflowParameters = null;
			BPMService bonitaService = autoInternal(workflowParameters, dataproc, dataId, itsmProc, pgenRequest, activeUser);
			dataProcService.refreshDataProc(dataproc);

			Boolean internFlag = GenericUtils.containsProfileByCode(activeUser.getProfiles(),PgenConstants.PGEN_INTERN_PROFILE);
			workflowParameters = getWorkFlowParameters(dataproc, WorkflowConstants.ACTION_TYPE_AUTO_ITSM, pgenRequest.getRequestType(), pgenRequest.getPriority(), internFlag);
			boolean error =  workflowService.executeTask(bonitaService, dataproc.getIdInstance(), dataproc.getIdTaskInstance(), workflowParameters);
			if(error==true){
				 List<DataProcHistory> history = dataProcHistoryService.findAllDataProcHistorysForProcInternalId(dataproc);
				 dataProcHistoryService.deleteDataProcHistory(history.get(0));
				 PgenRequestController.bonitaStatus = "failure";
			 }
			dataproc.setStatus(localizedMessageService.findLocalizedMessage(TwisterConstants.STATUS_IN_PROGRESS));
			
			if(PgenRequestController.bonitaStatus.equals("failure")){
				dataproc.setStatus(localizedMessageService.findLocalizedMessage(code));
				dataproc.setVersion(version);
				dataproc.setOperLast(last_oper);
				 persistDataProc(dataproc, pgenRequest);			}
			else{
				dataProcService.refreshDataProc(dataproc);
			}
			log.info("End - retry auto ITSM Creation");

		}

	}

	@Transactional(isolation = Isolation.DEFAULT, propagation=Propagation.REQUIRES_NEW)
	private BPMService autoInternal( Map<String,Object> workflowParameters, DataProc dataproc, Long dataId,final DataProc itsmProc,  final PgenRequest pgenRequest, Operator activeUser){

		dataproc.setOperLast(activeUser);
		dataproc.setDteLastAction(new Date());

		dataproc.setStatus(localizedMessageService.findLocalizedMessage(TwisterConstants.STATUS_ITSM_SENT));
		log.info("Request updated!");
		persistHistory(dataproc, ITwisterStatus.HISTORY_DETAILS_ITSM_AUTO);
		//If the user is a flag, send the info to bonita

		log.info("Start - Send Request to BPM");

		BPMService bonitaService = new BPMService();
		return bonitaService;
	}


}	
