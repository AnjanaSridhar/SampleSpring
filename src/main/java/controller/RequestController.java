package controller;

import java.security.Principal;

import javax.validation.Valid;

import model.DataProc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.support.SessionStatus;

import service.MyRequestService;
import domain.Myrequest;


@RequestMapping("/myRequests")
@Controller
public class RequestController {

	private String returnUpdateView = "pgenrequests/update";
	
	public static String bonitaStatus = "success";
	@Autowired private MyRequestService myRequestService;
	/*
	 * Binders & Validation
	 */

	/** Initialize the Binders and set the Validator corresponding to pgenRequestDto<br>
	 * @param binder - WebDataBinder <br>
	 */
/*	@InitBinder(TwisterConstants.PGEN_REQUEST_DTO)
	public void initBinder(WebDataBinder binder) {
		log.debug("Init Binder: "+binder.getObjectName());
		binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
	}

	
	 * Populate the new model in the view and initiate SessionAttributes conversation
	 
	


	
	 * Reference Data
	 

	*//** Populate Reference Data <br>
	 * @param uiModel - Model, contains attributes need it to display the data correctly in the jsp<br>
	 * @param operator - Operator to get the data in the corresponding language selected<br>
	 *//*
	public void populateReferenceData(Model uiModel, Operator operator){
		
		uiModel.addAttribute("listRequestTypes", processRefService.findAllPgenProcesses(operator));
		uiModel.addAttribute("listPriorities", listValueService.findAllListValueByListName(PgenConstants.PGEN_PRIORITY_LIST));
	}

	*//*****************
	 * FORM CREATION *
	 *****************//*
	
	 * CREATE
	 
	*//**
	 * Initialization of the form, first creation when /create is called
	 * 
	 * @param loggedUser - User connected to TWISTER
	 * @param uiModel - Model
	 * @return the view "pgenrequests/create" 
	 *//*
	@PreAuthorize("hasPermission(#loggedUser,'PGEN_CREATE')")
	@RequestMapping(value = "create", method = RequestMethod.GET, produces = TwisterConstants.PRODUCES_TEXT_HTML)
	public String createForm(Principal loggedUser,Model uiModel){
		logSecurity.debug(loggedUser.getName()+" starts Create Pgen Request Form");
		DataProc dataProc = new DataProc();
		ProcessRef selectedProcess = processRefService.findProcessRef(ProcessRefConstants.PROCESS_PGEN);
		ProcessTaskVersion ptv = processTaskVersionService.findActiveProcessTaskVersion(selectedProcess.getCode(),TaskRefConstants.CODE_CREATION);
		dataProc.setProcessTaskVersion(ptv);
		Operator creator = operatorService.findOperatorByLogin(loggedUser.getName());
		dataProc.setOperCreator(creator);
		PgenRequest newRequest = new PgenRequest(loggedUser.getName());
		populateEditForm(uiModel,dataProc,newRequest,creator);
		if(bonitaStatus.equals("failure")){
			uiModel.addAttribute("bonitafailure", "true");
			bonitaStatus = "success";
		}
		else{
			uiModel.addAttribute("bonitafailure", "");
		}
		return "pgenrequests/create";
	}

	
	 * UPDATE
	 


	*//**
	 * Get the <b>Update View</b> of the Request Created called from the  Todo requests list
	 * 
	 * @param dataProcId - (Path Variable parameter) the Id of the dataProc in request
	 * @param uiModel - Model
	 * @param loggedUser - User connected to TWISTER
	 * @return the update view "pgenrequests/update"
	 *//*
	@RequestMapping(value = "update/{dataProcId}", method = RequestMethod.GET, produces = TwisterConstants.PRODUCES_TEXT_HTML)
	public String updateForm(@PathVariable("dataProcId") Long dataProcId, Principal loggedUser, Model uiModel){
		logSecurity.debug(loggedUser.getName()+" starts Update Pgen Request Form - request ID ["+dataProcId+"]");
		log.info("Start - Retrieving data for modification form");
		DataProc updateProc = dataProcService.findDataProc(dataProcId);
		
		 * Deserialize procdata to pgenRequestDto
		 
		PgenRequest updateRequest = deserializeProcData(updateProc.getProcData(), PgenRequest.class);
		Operator creator = operatorService.findOperatorByLogin(updateProc.getOperCreator().getLogin());
		populateEditForm(uiModel,updateProc,updateRequest, creator);
		populateHistory(uiModel,updateProc);

		if(bonitaStatus.equals("failure")){
			uiModel.addAttribute("bonitafailure", "true");
			bonitaStatus = "success";
		}
		else{
			uiModel.addAttribute("bonitafailure", "");
		}
		log.info("End - data binded to view");
		return returnUpdateView;
	}

	
	 * VALIDATE
	 

	*//**
	 * Get the <b>Validate View</b> of the Request Created called from the  myRequests list
	 * 
	 * @param loggedUser - User connected to TWISTER
	 * @param dataProcId - (Path Variable parameter) the Id of the dataProc in request
	 * @param uiModel - Model
	 * @return the validate view "pgenrequests/validate"
	 *//*
	@RequestMapping(value = "validate/{dataProcId}", method = RequestMethod.GET, produces = TwisterConstants.PRODUCES_TEXT_HTML)
	public String validateForm(Principal loggedUser, @PathVariable("dataProcId") Long dataProcId, Model uiModel){
		logSecurity.debug(loggedUser.getName()+" starts Validate Pgen Request Form - request ID ["+dataProcId+"]");
		log.info("Start - Retrieving data for validation form");
		DataProc validateProc = dataProcService.findDataProc(dataProcId);
		
		 * Deserialize procdata to themisAccountDto
		 
		PgenRequest validateRequest = deserializeProcData(validateProc.getProcData(), PgenRequest.class);
		Operator validator = operatorService.findOperatorByLogin(validateProc.getOperCreator().getLogin());
		populateEditForm(uiModel,validateProc,validateRequest, validator);
		populateHistory(uiModel,validateProc);
		log.info("End - data binded to view");
		if(bonitaStatus.equals("failure")){
			uiModel.addAttribute("bonitafailure", "true");
			bonitaStatus = "success";
		}
		else{
			uiModel.addAttribute("bonitafailure", "");
		}
		return "pgenrequests/validate";
	}*/

	/*
	 * CREATE
	 */   
	
	/** <b>getNewDataProcDto</b>
	 * @return DataProc
	 */
	@ModelAttribute("dataProcdto")
	public DataProc getNewDataProcDto(){
		return new DataProc();
	}

	/** <b>getNewPgenRequestDto</b>
	 * @return PgenRequest
	 */
	@ModelAttribute("requestDto")
	public Myrequest getNewPgenRequestDto(){
		return new Myrequest();
	}

	/**
	 * Once the form is submitted with "Submit" button
	 * Validation of the bean with JSR-303
	 * 
	 * @param loggedUser - User connected to TWISTER
	 * @param createProc - DataProcDto to be created
	 * @param pgenRequest - PgenRequest to be created
	 * @param bindingResult - BindingResult contains errors if any after validation of the form 
	 * @param uiModel - Model
	 * @param status - SessionStatus
	 * @return myRequest PGEN search page if no errors during the validation
	 * @return The request update view "pgenrequests/update" with the errors to be corrected
	 */
	//@PreAuthorize("hasPermission(#loggedUser,'PGEN__CREATE')")
	@RequestMapping(value = "processForm", params="create", method = RequestMethod.POST)
	public String create(Principal loggedUser, @ModelAttribute("dataProcdto") final DataProc createProc, 
			@ModelAttribute("requestDto") @Valid final Myrequest myRequest, BindingResult bindingResult,
			Model uiModel, SessionStatus status){
		//log.info("start request");
		/*Operator activeUser = operatorService.findOperatorByLogin(loggedUser.getName());
		if (bindingResult.hasErrors()) {
			log.debug("errors: " + bindingResult.getAllErrors().toString());
			populateEditForm(uiModel,createProc,pgenRequest, activeUser);
			if(createProc.getTask() != null && !(createProc.getTask().getCode().equals(TaskRefConstants.CODE_CREATION) || createProc.getTask().getCode().equals(TaskRefConstants.CODE_DRAFT)) ){
				populateHistory(uiModel,createProc);
			}
			if(pgenRequest.getComment() != null && pgenRequest.getComment().length() > TwisterConstants.COMMENT_SIZE_MAX){
				uiModel.addAttribute(TwisterConstants.ERROR,2);
			}

			return returnUpdateView;
		}*/
		//boolean isCreate = ( createProc.getIdTaskInstance()==null) ? true:false ;
		
		myRequestService.create(createProc, myRequest, status);
		//log.info("sending request");
		uiModel.asMap().clear();
		status.setComplete();
		
		return "redirect:"+"myrequests/update"+"/"+createProc.getDataId();
	}



	/*
	 * Correct CCSIT
	 */   

	/**
	 * Once the form is submitted with "Ask CCSIT Correction" button
	 * 
	 * @param loggedUser - User connected to TWISTER
	 * @param correctProc -  DataProcDto to be corrected
	 * @param pgenRequest - PgenRequest to be corrected
	 * @param uiModel - Model
	 * @param status - SessionStatus
	 * @return myRequest PGEN search page
	 */
	/*@PreAuthorize("hasPermission(#loggedUser,'PGEN__ASK_EXECUTE_CCSIT')")
	@RequestMapping(value = TwisterConstants.PROCESS_FORM, params="correct", method = RequestMethod.POST)
	public String correct(Principal loggedUser, @ModelAttribute(TwisterConstants.DATA_PROC_DTO) final DataProc correctProc, 
			@ModelAttribute(TwisterConstants.PGEN_REQUEST_DTO) final PgenRequest pgenRequest, BindingResult bindingResult,
			Model uiModel, SessionStatus status){
		log.info(PgenConstants.LOG_START_SEND_REQUEST);

		pgenRequestService.correct(loggedUser, correctProc, pgenRequest);
		log.info(PgenConstants.LOG_END_SEND_REQUEST);
		uiModel.asMap().clear();
		status.setComplete();
		if(bonitaStatus.equals("failure")){
			return TwisterConstants.REDIRECT_VIEW+INavigationConstants.PGEN_REQUESTS+INavigationConstants.PAGE_ACTION_UPDATE+"/"+correctProc.getId();
		}
		else{
			return TwisterConstants.REDIRECT_VIEW+INavigationConstants.MAIN_MY_REQUEST_PGEN_LIST_PAGE;
		}
	}

	
	 * EXECUTE
	    
	*//**
	 * Once the form is submitted with "Accept Request" button
	 * 
	 * @param executeProc - DataProcDto to be executed
	 * @param pgenRequest - PgenRequest to be executed
	 * @param bindingResult - BindingResult contains errors if any 
	 * @param loggedUser - User connected to TWISTER
	 * @param uiModel - Model
	 * @param status - SessionStatus
	 * @return myRequest PGEN search page
	 *//*
	@PreAuthorize("hasPermission(#loggedUser,'PGEN__EXECUTE')")
	@RequestMapping(value = TwisterConstants.PROCESS_FORM, params="execute", method = RequestMethod.POST)
	public String execute(@ModelAttribute(TwisterConstants.DATA_PROC_DTO) final DataProc executeProc, 
			@ModelAttribute(TwisterConstants.PGEN_REQUEST_DTO) final PgenRequest pgenRequest, BindingResult bindingResult,
			Principal loggedUser,
			Model uiModel, SessionStatus status){
		log.info(PgenConstants.LOG_START_SEND_REQUEST);
		Operator activeUser = operatorService.findOperatorByLogin(loggedUser.getName());
		pgenRequestService.execute(executeProc, pgenRequest, activeUser);
		log.info(PgenConstants.LOG_END_SEND_REQUEST);
		uiModel.asMap().clear();
		status.setComplete();
		if(bonitaStatus.equals("failure")){
			uiModel.addAttribute("bonitafailure", "true");
			return TwisterConstants.REDIRECT_VIEW+INavigationConstants.PGEN_REQUESTS+INavigationConstants.PAGE_ACTION_UPDATE+"/"+executeProc.getId();
		}
		else{
			uiModel.addAttribute("bonitafailure", "");
			bonitaStatus = "success";
			return TwisterConstants.REDIRECT_VIEW+INavigationConstants.MAIN_MY_REQUEST_PGEN_LIST_PAGE;
		}
	}


	
	 * PGEN Completion
	 
	*//**
	 * Once the form is submitted with "Complete Request" button 
	 * 
	 * @param executeProc - DataProcDto to be completed
	 * @param pgenRequest - PgenRequest to be completed
	 * @param bindingResult - BindingResult contains errors if any 
	 * @param loggedUser - User connected to TWISTER
	 * @param uiModel - Model
	 * @param status - SessionStatus
	 * @return myRequest PGEN search page
	 *//*
	@PreAuthorize("hasPermission(#loggedUser,'PGEN__COMPLETE')")
	@RequestMapping(value = TwisterConstants.PROCESS_FORM, params="complete", method = RequestMethod.POST)
	public String complete(@ModelAttribute(TwisterConstants.DATA_PROC_DTO) final DataProc executeProc, 
			@ModelAttribute(TwisterConstants.PGEN_REQUEST_DTO) final PgenRequest pgenRequest, BindingResult bindingResult,
			Principal loggedUser,
			Model uiModel, SessionStatus status){
		log.info(PgenConstants.LOG_START_SEND_REQUEST);

		//If the user is a flag, send the info to bonita
		Operator activeUser = operatorService.findOperatorByLogin(loggedUser.getName());
		pgenRequestService.complete(executeProc, pgenRequest, activeUser);
		log.info(PgenConstants.LOG_END_SEND_REQUEST);
		uiModel.asMap().clear();
		status.setComplete();
		if(bonitaStatus.equals("failure")){
			uiModel.addAttribute("bonitafailure", "true");
			return TwisterConstants.REDIRECT_VIEW+INavigationConstants.PGEN_REQUESTS+INavigationConstants.PAGE_ACTION_UPDATE+"/"+executeProc.getId();
		}
		else{
			uiModel.addAttribute("bonitafailure", "");
			bonitaStatus = "success";
			return TwisterConstants.REDIRECT_VIEW+INavigationConstants.MAIN_MY_REQUEST_PGEN_LIST_PAGE;
		}
	}

	
	 * PGEN Ask More Info
	 
	*//**
	 * Once the form is submitted with "Ask for more information" button 
	 * 
	 * @param moreInfoProc - dataProc to be modified and complete the Information
	 * @param pgenRequest - PgenRequest to be modified
	 * @param bindingResult - BindingResult contains errors if any 
	 * @param loggedUser - User connected to TWISTER
	 * @param uiModel - Model
	 * @param status - SessionStatus
	 * @return myRequest PGEN search page
	 *//*
	@PreAuthorize("hasPermission(#loggedUser,'PGEN__ASK_MODIFICATION')")
	@RequestMapping(value = TwisterConstants.PROCESS_FORM, params="ask_info", method = RequestMethod.POST)
	public String moreInfo(@ModelAttribute(TwisterConstants.DATA_PROC_DTO) final DataProc moreInfoProc, 
			@ModelAttribute(TwisterConstants.PGEN_REQUEST_DTO) final PgenRequest pgenRequest, BindingResult bindingResult,
			Principal loggedUser,
			Model uiModel, SessionStatus status){
		log.info(PgenConstants.LOG_START_SEND_REQUEST);


		//If the user is a flag, send the info to bonita
		Operator activeUser = operatorService.findOperatorByLogin(loggedUser.getName());
		pgenRequestService.moreInfo(moreInfoProc, pgenRequest, activeUser);
		log.info(PgenConstants.LOG_END_SEND_REQUEST);
		uiModel.asMap().clear();
		status.setComplete();
		if(bonitaStatus.equals("failure")){
			uiModel.addAttribute("bonitafailure", "true");
			return TwisterConstants.REDIRECT_VIEW+INavigationConstants.PGEN_REQUESTS+INavigationConstants.PAGE_ACTION_UPDATE+"/"+moreInfoProc.getId();
		}
		else{
			uiModel.addAttribute("bonitafailure", "");
			bonitaStatus = "success";
			return TwisterConstants.REDIRECT_VIEW+INavigationConstants.MAIN_MY_REQUEST_PGEN_LIST_PAGE;
		}
	}


	
	 * CLOSE
	    
	*//**
	 * Once the form is submitted with "Close Request" button 
	 * 
	 * @param closeProc - DataProc to be closed
	 * @param pgenRequest - PgenRequest to be closed
	 * @param bindingResult - BindingResult contains errors if any 
	 * @param loggedUser - User connected to TWISTER
	 * @param uiModel - Model
	 * @param status - SessionStatus
	 * @return myRequest PGEN search page
	 *//*
	@PreAuthorize("hasPermission(#loggedUser,'PGEN__CLOSE')")
	@RequestMapping(value = TwisterConstants.PROCESS_FORM, params="close", method = RequestMethod.POST)
	public String close(@ModelAttribute(TwisterConstants.DATA_PROC_DTO) final DataProc closeProc, 
			@ModelAttribute(TwisterConstants.PGEN_REQUEST_DTO) final PgenRequest pgenRequest, BindingResult bindingResult,
			Principal loggedUser,
			Model uiModel, SessionStatus status){
		log.info(PgenConstants.LOG_START_SEND_REQUEST);

		//If the user is a flag, send the info to bonita
		Operator activeUser = operatorService.findOperatorByLogin(loggedUser.getName());
		pgenRequestService.close(closeProc, pgenRequest, activeUser);
		log.info(PgenConstants.LOG_END_SEND_REQUEST);
		uiModel.asMap().clear();
		status.setComplete();
		if(bonitaStatus.equals("failure")){
			uiModel.addAttribute("bonitafailure", "true");
			return TwisterConstants.REDIRECT_VIEW+INavigationConstants.PGEN_REQUESTS+INavigationConstants.PAGE_ACTION_UPDATE+"/"+closeProc.getId();
		}
		else{
			uiModel.addAttribute("bonitafailure", "");
			bonitaStatus = "success";
			return TwisterConstants.REDIRECT_VIEW+INavigationConstants.MAIN_MY_REQUEST_PGEN_LIST_PAGE;
		}
	}

	
	 * REJECT
	    
	*//**
	 * Once the form is submitted with "Reject" button 
	 * 
	 * @param rejectProc - DataProc to be rejected
	 * @param pgenRequest - PgenRequest to be rejected
	 * @param bindingResult - BindingResult contains errors if any 
	 * @param loggedUser - User connected to TWISTER
	 * @param uiModel - Model
	 * @param status - SessionStatus
	 * @return myRequest PGEN search page
	 *//*
	@PreAuthorize("hasPermission(#loggedUser,'PGEN__REJECT')")
	@RequestMapping(value = TwisterConstants.PROCESS_FORM, params="reject", method = RequestMethod.POST)
	public String reject(@ModelAttribute(TwisterConstants.DATA_PROC_DTO) final DataProc rejectProc, 
			@ModelAttribute(TwisterConstants.PGEN_REQUEST_DTO) final PgenRequest pgenRequest, BindingResult bindingResult,
			Principal loggedUser,
			Model uiModel, SessionStatus status){
		log.info(PgenConstants.LOG_START_SEND_REQUEST);

		//If the user is a flag, send the info to bonita
		Operator activeUser = operatorService.findOperatorByLogin(loggedUser.getName());
		pgenRequestService.reject(rejectProc, pgenRequest, activeUser);
		log.info(PgenConstants.LOG_END_SEND_REQUEST);
		uiModel.asMap().clear();
		status.setComplete();
		if(bonitaStatus.equals("failure")){
			uiModel.addAttribute("bonitafailure", "true");
			return TwisterConstants.REDIRECT_VIEW+INavigationConstants.PGEN_REQUESTS+INavigationConstants.PAGE_ACTION_UPDATE+"/"+rejectProc.getId();
		}
		else{
			uiModel.addAttribute("bonitafailure", "");
			bonitaStatus = "success";
			return TwisterConstants.REDIRECT_VIEW+INavigationConstants.MAIN_MY_REQUEST_PGEN_LIST_PAGE;
		}
	}

	
	 * ABANDON
	    
	*//**
	 * Once the form is submitted with "Abandon" button 
	 * 
	 * @param abandonProc  - DataProc to be abandonated
	 * @param pgenRequest - PgenRequest to be abandonated
	 * @param bindingResult - BindingResult contains errors if any 
	 * @param loggedUser - User connected to TWISTER
	 * @param uiModel - Model
	 * @param status - SessionStatus
	 * @return myRequest PGEN search page
	 *//*
	@PreAuthorize("hasPermission(#loggedUser,'PGEN__ABANDON')")
	@RequestMapping(value = TwisterConstants.PROCESS_FORM, params="abandon", method = RequestMethod.POST)
	public String abandon(@ModelAttribute(TwisterConstants.DATA_PROC_DTO) final DataProc abandonProc, 
			@ModelAttribute(TwisterConstants.PGEN_REQUEST_DTO) final PgenRequest pgenRequest, BindingResult bindingResult,
			Principal loggedUser,
			Model uiModel, SessionStatus status){
		log.info(PgenConstants.LOG_START_SEND_REQUEST);

		//If the user is a flag, send the info to bonita
		Operator activeUser = operatorService.findOperatorByLogin(loggedUser.getName());
		pgenRequestService.abandon(abandonProc, pgenRequest, activeUser);
		log.info(PgenConstants.LOG_END_SEND_REQUEST);
		uiModel.asMap().clear();
		status.setComplete();
		if(bonitaStatus.equals("failure")){
			uiModel.addAttribute("bonitafailure", "true");
			return TwisterConstants.REDIRECT_VIEW+INavigationConstants.PGEN_REQUESTS+INavigationConstants.PAGE_ACTION_UPDATE+"/"+abandonProc.getId();
		}
		else{
			uiModel.addAttribute("bonitafailure", "");
			bonitaStatus = "success";
			return TwisterConstants.REDIRECT_VIEW+INavigationConstants.MAIN_MY_REQUEST_PGEN_LIST_PAGE;
		}
	}

	
	 * CONTROL
	    
	*//**
	 * Once the form is submitted with "Processing done" button 
	 * 
	 * @param controlProc - DataProc to be controled
	 * @param pgenRequest - PgenRequest to be controled
	 * @param bindingResult - BindingResult contains errors if any 
	 * @param loggedUser - User connected to TWISTER
	 * @param uiModel - Model
	 * @param status - SessionStatus
	 * @return myRequest PGEN search page
	 *//* 
	@PreAuthorize("hasPermission(#loggedUser,'PGEN__CONTROL')")
	@RequestMapping(value = TwisterConstants.PROCESS_FORM, params="control", method = RequestMethod.POST)
	public String control(@ModelAttribute(TwisterConstants.DATA_PROC_DTO) final DataProc controlProc, 
			@ModelAttribute(TwisterConstants.PGEN_REQUEST_DTO) final PgenRequest pgenRequest, BindingResult bindingResult,
			Principal loggedUser,
			Model uiModel, SessionStatus status){
		log.info(PgenConstants.LOG_START_SEND_REQUEST);

		//If the user is a flag, send the info to bonita
		Operator activeUser = operatorService.findOperatorByLogin(loggedUser.getName());

		pgenRequestService.control(controlProc, pgenRequest, activeUser);
		log.info(PgenConstants.LOG_END_SEND_REQUEST);
		uiModel.asMap().clear();
		status.setComplete();
		if(bonitaStatus.equals("failure")){
			uiModel.addAttribute("bonitafailure", "true");
			return TwisterConstants.REDIRECT_VIEW+INavigationConstants.PGEN_REQUESTS+INavigationConstants.PAGE_ACTION_UPDATE+"/"+controlProc.getId();
		}
		else{
			uiModel.addAttribute("bonitafailure", "");
			bonitaStatus = "success";
			return TwisterConstants.REDIRECT_VIEW+INavigationConstants.MAIN_MY_REQUEST_PGEN_LIST_PAGE;
		}
		
	}

	
	 * VALIDATE
	    
	*//**
	 * Once the form is submitted with "Validate" button 
	 * 
	 * @param loggedUser - User connected to TWISTER
	 * @param validateProc - DataProc to be validated
	 * @param pgenRequest - PgenRequest to be validated
	 * @param bindingResult - BindingResult contains errors if any 
	 * @param uiModel - Model
	 * @param status - SessionStatus
	 * @return The update request view with the error to be corrected before resubmitting the form to validate
	 * @return myRequest PGEN search page if no error 
	 *//*
	@PreAuthorize("hasPermission(#loggedUser,'PGEN__VALIDATE')")
	@RequestMapping(value = TwisterConstants.PROCESS_FORM, params="validate", method = RequestMethod.POST)
	public String validate(Principal loggedUser, @ModelAttribute(TwisterConstants.DATA_PROC_DTO) final DataProc validateProc, 
			@ModelAttribute(TwisterConstants.PGEN_REQUEST_DTO) @Valid final PgenRequest pgenRequest, BindingResult bindingResult,
			Model uiModel, SessionStatus status){
		log.info(PgenConstants.LOG_START_SEND_REQUEST);
		if (bindingResult.hasErrors()) {
			log.debug("errors: " + bindingResult.getAllErrors().toString());
			Operator creator = operatorService.findOperatorByLogin(validateProc.getOperCreator().getLogin());
			populateEditForm(uiModel,validateProc,pgenRequest, creator);
			populateFiles(uiModel,validateProc, pgenRequest);
			if(validateProc.getTask() != null && !(validateProc.getTask().getCode().equals(TaskRefConstants.CODE_CREATION) || validateProc.getTask().getCode().equals(TaskRefConstants.CODE_DRAFT)) ){
				populateHistory(uiModel,validateProc);
			}
			if(pgenRequest.getComment() != null && pgenRequest.getComment().length() > TwisterConstants.COMMENT_SIZE_MAX){
				uiModel.addAttribute(TwisterConstants.ERROR,2);
			}
			return returnUpdateView;
		}
		Operator activeUser = operatorService.findOperatorByLogin(loggedUser.getName());

		pgenRequestService.validate(validateProc, pgenRequest, activeUser);
		
		//If the user is a flag, send the info to bonita

		log.info(PgenConstants.LOG_END_SEND_REQUEST);
		uiModel.asMap().clear();
		status.setComplete();
		if(bonitaStatus.equals("failure")){
			uiModel.addAttribute("bonitafailure", "true");
			return TwisterConstants.REDIRECT_VIEW+INavigationConstants.PGEN_REQUESTS+INavigationConstants.PAGE_ACTION_UPDATE+"/"+validateProc.getId();
		}
		else{
			uiModel.addAttribute("bonitafailure", "");
			bonitaStatus = "success";
			return TwisterConstants.REDIRECT_VIEW+INavigationConstants.MAIN_MY_REQUEST_PGEN_LIST_PAGE;
		}
	}


	
	 * CANCEL
	 
	*//**
	 * Once the form is submitted with "Cancel" button 
	 * 
	 * @param status - SessionStatus
	 * @return Todo main search page
	 *//*
	@RequestMapping(value = TwisterConstants.PROCESS_FORM, params="cancel", method = RequestMethod.POST)
	public String cancel(SessionStatus status) {
		log.info("Cancel Action -> Redirect to main page");
		status.setComplete();
		return TwisterConstants.REDIRECT_VIEW+INavigationConstants.MAIN_TODO_LIST_PAGE;
	}


	public void populateEditForm(Model uiModel, DataProc dataProc, PgenRequest pgenRequest, Operator operator) {
		populateReferenceData(uiModel, operator);
		super.populateEditForm(uiModel, dataProc, pgenRequest);
	}

	
	 * Manual ITSM
	    

	*//**
	 * Once the form is submitted with "Send Manual ITSM" button
	 * 
	 * @param itsmProc - DataProc to which the itsm should be created
	 * @param bindingResult - BindingResult contains errors if any 
	 * @param pgenRequest - PgenRequest
	 * @param principal - Operator logged to Twister
	 * @param uiModel - Model
	 * @param status - SessionStatus
	 * @param selectedLocale - Locale, the language selected
	 * @return in case of Itsm not created return the "pgenrequests/update" view
	 * @return myRequests DataProcs search page
	 *//*
	@PreAuthorize("hasPermission(#loggedUser,'PGEN__RETRY_ITSM_MANUAL')")
	@RequestMapping(value = "retryItsm", method = RequestMethod.POST, produces = TwisterConstants.PRODUCES_TEXT_HTML)
	public String manual(@ModelAttribute(TwisterConstants.DATA_PROC_DTO) @Valid final DataProc itsmProc, BindingResult bindingResult,
			@ModelAttribute(TwisterConstants.PGEN_REQUEST_DTO) @Valid final PgenRequest pgenRequest,
			Principal principal,
			Model uiModel, SessionStatus status, Locale selectedLocale){
		log.info("Start - manual ITSM Creation");

		Operator user = operatorService.findOperatorByLogin(itsmProc.getOperCreator().getLogin());
		DataProc dataProc =dataProcService.findDataProc(itsmProc.getId());
		if(dataProc.getItsmNumber()==null)
		{
			if(!isItsmValid(itsmProc, principal.getName(), pgenRequest)){
				bindingResult.rejectValue("itsmNumber", "itsmNumber.code.NotNull");
				populateEditForm(uiModel,itsmProc,pgenRequest, user);
				populateHistory(uiModel, itsmProc);
				return returnUpdateView;
			}
		}
		else
		{
			//FIXME - ALTI.NMA - remove when lock request 
			//ITSM already created
			uiModel.addAttribute(TwisterConstants.ERROR,3);
			populateEditForm(uiModel,dataProc,pgenRequest, user);
			return returnUpdateView;
			//flash message or a PopUp "ITSM already created"
		}
		uiModel.asMap().clear();   
		status.setComplete();
		if(bonitaStatus.equals("failure")){
			uiModel.addAttribute("bonitafailure", "true");
			return TwisterConstants.REDIRECT_VIEW+INavigationConstants.PGEN_REQUESTS+INavigationConstants.PAGE_ACTION_UPDATE+"/"+itsmProc.getId();
		}
		else{
			uiModel.addAttribute("bonitafailure", "");
			bonitaStatus = "success";
			return TwisterConstants.REDIRECT_VIEW+INavigationConstants.MAIN_MY_REQUEST_PGEN_LIST_PAGE;
		}
	}

	
	 * Create AUTO ITSM
	 

	*//**
	 * Function Called once the "Auto" button is submitted
	 * Automatically Create the ITSM number for this DataProc
	 * 
	 * @param dataId - Id of the DataProc to which the itsm should be created
	 * @param itsmProc - DataProc
	 * @param pgenRequest - PgenRequest
	 * @param loggedUser - User logged to Twister
	 * @param uiModel - Model
	 * @param status - SessionStatus
	 * @return Todo dataProcs search page
	 *//*
	@RequestMapping(value = "retryItsm/{dataProcId}", method = RequestMethod.GET, produces = TwisterConstants.PRODUCES_TEXT_HTML)
	public String auto(@PathVariable("dataProcId") Long dataId, @ModelAttribute(TwisterConstants.DATA_PROC_DTO) final DataProc itsmProc, @ModelAttribute(TwisterConstants.PGEN_REQUEST_DTO) final PgenRequest pgenRequest,
			Principal loggedUser,
			Model uiModel, SessionStatus status){
		log.info("Start - retry auto ITSM Creation");
		uiModel.asMap().clear();
		Operator user = operatorService.findOperatorByLogin(loggedUser.getName());
		pgenRequestService.auto(dataId, itsmProc, pgenRequest, user);
		status.setComplete();
		if(bonitaStatus.equals("failure")){
			uiModel.addAttribute("bonitafailure", "true");
			return TwisterConstants.REDIRECT_VIEW+INavigationConstants.PGEN_REQUESTS+INavigationConstants.PAGE_ACTION_UPDATE+"/"+itsmProc.getId();
		}
		else{
			uiModel.addAttribute("bonitafailure", "");
			bonitaStatus = "success";
			return TwisterConstants.REDIRECT_VIEW+INavigationConstants.MAIN_MY_REQUEST_PGEN_LIST_PAGE;
		}
	}

	
	 * I18N Reload
	 

	*//**
	 * When Reloading the page
	 * 
	 * @param dataProc - DataProc
	 * @param pgenRequest - PgenRequest
	 * @param bindingResult - BindingResult contains errors if any 
	 * @param loggedUser - User connected to TWISTER
	 * @param uiModel - Model
	 * @param status - SessionStatus
	 * @return the request update view "pgenrequests/update"
	 *//*
	@RequestMapping(value = TwisterConstants.PROCESS_FORM, method = RequestMethod.GET)
	public String reload(@ModelAttribute(TwisterConstants.DATA_PROC_DTO) final DataProc dataProc, 
			@ModelAttribute(TwisterConstants.PGEN_REQUEST_DTO) @Valid final PgenRequest pgenRequest, BindingResult bindingResult,
			Principal loggedUser, Model uiModel, SessionStatus status){
		String userLogin = null;
		if(dataProc.getOperCreator() != null){
			userLogin = dataProc.getOperCreator().getLogin();
		}
		else{
			userLogin = loggedUser.getName();
		}
		Operator user = operatorService.findOperatorByLogin(userLogin);
		populateEditForm(uiModel,dataProc,pgenRequest, user);
		if(dataProc.getTask() != null && !(dataProc.getTask().getCode().equals(TaskRefConstants.CODE_CREATION) || dataProc.getTask().getCode().equals(TaskRefConstants.CODE_DRAFT)) ){
			populateHistory(uiModel,dataProc);
		}
		if(pgenRequest.getComment() != null && pgenRequest.getComment().length() > TwisterConstants.COMMENT_SIZE_MAX){
			uiModel.addAttribute(TwisterConstants.ERROR,2);
		}
		return returnUpdateView;
	}*/
}
