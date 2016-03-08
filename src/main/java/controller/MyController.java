package controller;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;


/**
 * <b>PgenController</b> extends AbstractTwisterController to handle all PGEN common functions
 * 
 * Functions like:
 * - Save the dataProc History,
 * - Save the dataProc Files,
 * - Save the dataProc Comments,
 * - handle the Todo, myRequests and myDrafts search page
 * ...
 */
@Controller
public class MyController extends AbstractMyController {

	private Logger log = LoggerFactory.getLogger(MyController.class);
	private Logger logSecurity = LoggerFactory.getLogger("TwisterSec");

	@Autowired @Qualifier("pgenService") 
	private PgenService pgenService;
	/*
	 * Populate Functions
	 */

	/**
	 * populate Files funtion gets the files related to this DataProc from DATA_PROC_FILE table and populate it
	 *
	 * @param uiModel - Model
	 * @param dataProc - DataProc corresponding to the populated files
	 * @param pgenDto - PgenDto
	 */
	protected void populateFiles(Model uiModel, DataProc dataProc, PgenDto pgenDto){
		//Populate files already saved
		Set<DataProcFile> listFiles = new HashSet<DataProcFile>();
		if(!CollectionUtils.isEmpty(dataProc.getFiles())){
			listFiles.addAll(dataProc.getFiles());
		}

		//Populate files newly added
		List<DataProcFile> sFiles = dataProcFileService.findAllDataProcFilesByRequestId(pgenDto.getRequestId());
		listFiles.addAll(sFiles);
		uiModel.addAttribute("listFiles", listFiles);
	}


	/**
	 * Populate History function gets the history corresponding to this DataProc from DATA_PROC_HISTORY table to be populated
	 * 
	 * @param uiModel - Model
	 * @param dataProc - history related to this DataProc
	 */
	protected void populateHistory(Model uiModel, DataProc dataProc){
		List<DataProcHistoryDto> listHistory = dataProcHistoryService.findHistoryDtoByDataProc(dataProc);
		uiModel.addAttribute("listHistory", listHistory);
	}



	/*
	 * Persist Methods
	 */

	/**
	 * Fuction called to check if the ITSM is Valid
	 * 
	 * @param itsmDataProc - DataProc to which the itsm should be created
	 * @param operatorName - Operator logged to Twister
	 * @param pgenRequest - PgenRequest to which the itsm should be created
	 * @return <b>true</b> - if the itsm number is created
	 * @return <b>false</b> - if error
	 */

	public boolean isItsmValid(DataProc itsmDataProc, String operatorName, PgenRequest pgenRequest)
	{

		return pgenService.isItsmValid(itsmDataProc, operatorName, pgenRequest);

	}
	
	
	/**
	 * Populate the Form 
	 * Add Attributes in the Model Parameter like list of operations, loans, button actions, is the user logged a BO, is the DataProc in a validation phase, ... 
	 * these attributes are used to well display the jsp, corresponding to the status of the DataProc (like all fields are in readOnly if the attribute isValidation is True
	 * 
	 * @param uiModel - Model
	 * @param dataProc - DataProc to be populated
	 * @param pgenRequest - PgenRequest
	 */
	protected void populateEditForm(Model uiModel, DataProc dataProc, PgenRequest pgenRequest) {
		addDateTimeFormatPatterns(uiModel,"S-");
		uiModel.addAttribute(TwisterConstants.DATA_PROC_DTO, dataProc);
		uiModel.addAttribute("pgenRequestDto", pgenRequest);
		//see how to set the is validation for Pgen since we have a loop in the workflow 
		//we should find a way to disable the Request type
		Boolean isValidation = isFormValidation(dataProc.getTask());
		if(pgenRequest.getRequestType() != null){
			uiModel.addAttribute("selectedRequestType",pgenRequest.getRequestType());
		}
		if(pgenRequest.getPriority() != null){
			uiModel.addAttribute("selectedPriority",pgenRequest.getPriority());
		}

		List<TaskRight> buttons = getPgenButtonActions(dataProc);

		uiModel.addAttribute("buttonActions",buttons);
		if(buttons== null || buttons.size() == 0){
			uiModel.addAttribute("isValidation",Boolean.TRUE);
		}
		else  
		{
			uiModel.addAttribute("isValidation",isValidation);
		}

		uiModel.addAttribute("isItsmForm",isFormITSM(dataProc));
		uiModel.addAttribute("isHistoryVisible",isHistoryVisible(dataProc));
		uiModel.addAttribute("isHistoryNotXmlEscape",Boolean.TRUE);
		uiModel.addAttribute("requestId",pgenRequest.getRequestId());
	}


	/*
	 * Button Actions 
	 */
	/**
	 * Gets the Action Buttons corresponding on the Task Right of the user Connected and the status of the DataProc in question
	 * 
	 * @param dataProc - DataProc
	 * @return List of TaskRight 
	 */
	public List<TaskRight> getPgenButtonActions(DataProc dataProc) {
		List<TaskRight> result = null;
		Set<Profile> curProfiles = null;
		//if the BPM didn't finish processing its part then we block any action
		if(dataProc.getStatus() == null || !dataProc.getStatus().getCode().equals(TwisterConstants.STATUS_IN_PROGRESS)){

			String loggedUser = SecurityContextHolder.getContext().getAuthentication().getName();
			Operator currOpe = operatorService.findOperatorByLogin(loggedUser);

			//when creating the Request the entity is Null
			if(dataProc.getEntity()==null)
			{
				curProfiles = currOpe.getProfiles();

			}
			else
			{
				//Search for User Profile for this specific dataProc Entity
				List<Profile> profilesList = profileService.findProfileByOperatorAndEntity(currOpe, dataProc.getEntity());
				curProfiles = new HashSet<Profile>(profilesList);
			}

			//find all taskRights for these Profiles and ProcessTask
			if(dataProc.getProcessTaskVersion() != null){
				result = taskRightService.findAvailableActionsByProfilesAndProcessTask(curProfiles, dataProc.getProcessTaskVersion().getId().getProcessTask());

			}
		}

		return result;
	}


	/*
	 * List
	 */

	/**
	 * Get Todo PGEN Search Page 
	 * 
	 * @param loggedUser - User connected to TWISTER 
	 * @param uiModel - Model 
	 * @param selectedLocale - Locale, the language selected
	 * @return "pgenrequests/todo/search" Todo PGEN search page
	 */
	@RequestMapping(value = "todo", method = RequestMethod.GET, produces = TwisterConstants.PRODUCES_TEXT_HTML)
	public String searchTodoByProcess(Principal loggedUser, Model uiModel, Locale selectedLocale){
		logSecurity.debug(loggedUser.getName()+" starts a Search on Pgen ToDo ");
		populateSearchPage(loggedUser, uiModel, selectedLocale);
		uiModel.addAttribute("action", "resultTodo");
		return "pgenrequests/todo/search";
	}

	/**
	 * Get MyRequest PGEN Search page
	 * 
	 * @param loggedUser - User connected to TWISTER 
	 * @param uiModel - Model 
	 * @param selectedLocale - Locale, the language selected
	 * @return myRequests PGEN search page
	 */
	@RequestMapping(value = "myrequests", method = RequestMethod.GET, produces = TwisterConstants.PRODUCES_TEXT_HTML)
	public String listMyRequestsByProcess(Principal loggedUser, Model uiModel, Locale selectedLocale){
		logSecurity.debug(loggedUser.getName()+" starts a Search on Pgen My Requests ");
		populateSearchPage(loggedUser, uiModel, selectedLocale);
		uiModel.addAttribute("action", "resultMyRequest");
		return "pgenrequests/todo/search";
	}


	/**
	 * Get MyDrafts PGEN Search page
	 * 
	 * @param loggedUser - User connected to TWISTER 
	 * @param uiModel - Model 
	 * @param selectedLocale - Locale, the language selected
	 * @return MyDrafts PGEN search page
	 */
	@RequestMapping(value = "mydrafts", method = RequestMethod.GET, produces = TwisterConstants.PRODUCES_TEXT_HTML)
	public String listMyDraftsByProcess(Principal loggedUser, Model uiModel, Locale selectedLocale){
		logSecurity.debug(loggedUser.getName()+" starts a Search on Pgen My Drafts ");
		populateSearchPage(loggedUser, uiModel, selectedLocale);
		uiModel.addAttribute("action", "resultMyDrafts");
		return "pgenrequests/todo/search";
	}

	/**
	 * populate Todo, myRequest, myDraft SearchDto Form
	 * Add Attributes to the model necessary for the display of the search page like list of Processses, of entities
	 * 
	 * @param loggedUser - User connected to TWISTER 
	 * @param uiModel - Model 
	 * @param selectedLocale - Locale, the language selected
	 */
	private void populateSearchPage(Principal loggedUser, Model uiModel, Locale selectedLocale) {
		Operator logged = operatorService.findOperatorByLogin(loggedUser.getName());
		ToDoSearchRequest searchDto = new ToDoSearchRequest();
		uiModel.addAttribute("searchDto", searchDto);
		List<ProcessRefDto> processes = processRefService.findProcessesForOperatorAndCurrentProcess(logged.getCode(),PepiConstants.PEPI_SEARCH_PREFIX, selectedLocale);
		uiModel.addAttribute("listProcesses", processes);
		//Or List<Entity> entities = (List<Entity>) logged.getEntities();
		List<Entity> entities = entityService.findEntitiesByUserAndProcessId(logged, PgenConstants.PGEN_SEARCH_PREFIX);
		uiModel.addAttribute("listEntities", entities);
		addDateTimeFormatPatterns(uiModel,"S-");
	}

	/*
	 * CRITERIA
	 */

	/**
	 * Search for Todo Pgen Requests results based on the filter selected in search criteria page
	 * 
	 * @param principal - the user logged to Twister
	 * @param searchDto - the search filter criterias
	 * @param page - number of the page results
	 * @param size - size of the results in each page
	 * @param uiModel - Model 
	 * @return Todo Pgen requests results view base on the filter selected
	 */
	@RequestMapping(value="resultTodo",method = RequestMethod.POST, produces = TwisterConstants.PRODUCES_TEXT_HTML)
	public String search(Principal principal, @ModelAttribute("searchDto") ToDoSearchRequest searchDto, 
			@RequestParam(value = TwisterConstants.PAGE, required = false) Integer page, @RequestParam(value = TwisterConstants.SIZE, required = false) Integer size, Model uiModel){

		Operator curOperator = operatorService.findOperatorByLogin(principal.getName());
		logSecurity.debug(principal.getName()+" starts a Search on Pgen Todo by criteria");
		uiModel.addAttribute("searchDto", searchDto);
		if(searchDto.getProcess()!=null && searchDto.getProcess().getCode().equals(TwisterConstants.TODO_SEARCH_ALL_PROCESS))
		{
			searchDto.setProcess(null);
		}      
		if(searchDto.getEntity()!=null && searchDto.getEntity().getCode().equals(TwisterConstants.TODO_SEARCH_ALL_ENTITY))
		{
			searchDto.setEntity(null);
		}

		int sizeNo = size == null ? TwisterConstants.SEARCH_RESULT_LIST_SIZE_DEFAULT : size.intValue();
		final int firstResult = page == null ? 0 : (page.intValue() - 1) * sizeNo;
		List<DataProc> todos = dataProcService.findToDoBySearchCriteria(curOperator, PgenConstants.PGEN_SEARCH_PREFIX, searchDto, firstResult, sizeNo);
		TodoSearchListResultsDto  searchResults = getTodoSearchResultsDtoFromDataProcList(todos);
		uiModel.addAttribute("searchResults", searchResults);
		if(!CollectionUtils.isEmpty(searchResults.getResults())){
			float nrOfPages = (float) dataProcService.countAllToDoBySearchCriteria(curOperator, PgenConstants.PGEN_SEARCH_PREFIX, searchDto) / sizeNo;
			uiModel.addAttribute(TwisterConstants.MAX_PAGES, (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
		}

		addDateTimeFormatPatterns(uiModel,"S-");
		uiModel.addAttribute(TwisterConstants.SEARCH_PAGE, TwisterConstants.SEARCH_PAGE_TODO);
		PageAction pa = new PageAction(false,false,false,true,false);
		uiModel.addAttribute(TwisterConstants.PAGE_ACTION, pa); 
		return "pgenrequests/todo/result";
	}

	/**
	 * Search for MyRequests Pgen Requests results based on the filter selected in search criteria page
	 * 
	 * @param principal - the user logged to Twister
	 * @param searchDto - the search filter criterias
	 * @param page - number of the page results
	 * @param size - size of the results in each page
	 * @param uiModel - Model 
	 * @return MyRequests PGEN results view base on the filter selected
	 */
	@RequestMapping(value="resultMyRequest",method = RequestMethod.POST, produces = TwisterConstants.PRODUCES_TEXT_HTML)
	public String searchMyRequests(Principal principal, @ModelAttribute("searchDto") ToDoSearchRequest searchDto, 
			@RequestParam(value = TwisterConstants.PAGE, required = false) Integer page, @RequestParam(value = TwisterConstants.SIZE, required = false) Integer size, Model uiModel){

		Operator curOperator = operatorService.findOperatorByLogin(principal.getName());
		logSecurity.debug(principal.getName()+" starts a Search on Pgen Todo by criteria");
		uiModel.addAttribute("searchDto", searchDto);
		if(searchDto.getProcess()!=null && searchDto.getProcess().getCode().equals(TwisterConstants.TODO_SEARCH_ALL_PROCESS))
		{
			searchDto.setProcess(null);
		}      
		if(searchDto.getEntity()!=null && searchDto.getEntity().getCode().equals(TwisterConstants.TODO_SEARCH_ALL_ENTITY))
		{
			searchDto.setEntity(null);
		}

		int sizeNo = size == null ? TwisterConstants.SEARCH_RESULT_LIST_SIZE_DEFAULT : size.intValue();
		final int firstResult = page == null ? 0 : (page.intValue() - 1) * sizeNo;
		List<DataProc> requests = dataProcService.findMyReqBySearchCriteria(curOperator, PgenConstants.PGEN_SEARCH_PREFIX, searchDto, firstResult, sizeNo);
		TodoSearchListResultsDto  searchResults = getTodoSearchResultsDtoFromDataProcList(requests);
		uiModel.addAttribute("searchResults", searchResults);
		if(!CollectionUtils.isEmpty(searchResults.getResults())){
			float nrOfPages = (float) dataProcService.countAllMyReqBySearchCriteria(curOperator, PgenConstants.PGEN_SEARCH_PREFIX, searchDto) / sizeNo;
			uiModel.addAttribute(TwisterConstants.MAX_PAGES, (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
		}
		uiModel.addAttribute(TwisterConstants.SEARCH_PAGE, TwisterConstants.SEARCH_PAGE_MY_REQUEST);
		addDateTimeFormatPatterns(uiModel,"S-");
		//Update
		PageAction pa = new PageAction(false,false,true,false,false);
		uiModel.addAttribute(TwisterConstants.PAGE_ACTION, pa); 
		return "pgenrequests/todo/result";
	}

	/**
	 * Search for MyDrafts Pgen Requests results based on the filter selected in search criteria page
	 * 
	 * @param principal - the user logged to Twister
	 * @param searchDto - the search filter criterias
	 * @param page - number of the page results
	 * @param size - size of the results in each page
	 * @param uiModel - Model 
	 * @return MyDrafts PGEN results view base on the filter selected
	 */
	@RequestMapping(value="resultMyDrafts",method = RequestMethod.POST, produces = TwisterConstants.PRODUCES_TEXT_HTML)
	public String searchMyDrafts(Principal principal, @ModelAttribute("searchDto") ToDoSearchRequest searchDto,
			@RequestParam(value = TwisterConstants.PAGE, required = false) Integer page, @RequestParam(value = TwisterConstants.SIZE, required = false) Integer size, Model uiModel){

		Operator curOperator = operatorService.findOperatorByLogin(principal.getName());
		logSecurity.debug(principal.getName()+" starts a Search on Pgen Todo by criteria");

		uiModel.addAttribute("searchDto", searchDto);
		if(searchDto.getProcess()!=null && searchDto.getProcess().getCode().equals(TwisterConstants.TODO_SEARCH_ALL_PROCESS))
		{
			searchDto.setProcess(null);
		}      
		if(searchDto.getEntity()!=null && searchDto.getEntity().getCode().equals(TwisterConstants.TODO_SEARCH_ALL_ENTITY))
		{
			searchDto.setEntity(null);
		}

		int sizeNo = size == null ? TwisterConstants.SEARCH_RESULT_LIST_SIZE_DEFAULT : size.intValue();
		final int firstResult = page == null ? 0 : (page.intValue() - 1) * sizeNo;
		List<DataProc> drafts = dataProcService.findMyDraftsBySearchCriteria(curOperator, PgenConstants.PGEN_SEARCH_PREFIX, searchDto, firstResult, sizeNo);
		TodoSearchListResultsDto  searchResults = getTodoSearchResultsDtoFromDataProcList(drafts);
		uiModel.addAttribute("searchResults", searchResults);
		if(!CollectionUtils.isEmpty(searchResults.getResults())){
			float nrOfPages = (float) dataProcService.countMyDraftsBySearchCriteria(curOperator, PgenConstants.PGEN_SEARCH_PREFIX, searchDto) / sizeNo;
			uiModel.addAttribute(TwisterConstants.MAX_PAGES, (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
		}
		uiModel.addAttribute(TwisterConstants.SEARCH_PAGE, TwisterConstants.SEARCH_PAGE_MY_DRAFT);
		addDateTimeFormatPatterns(uiModel,"S-");
		//Update & Delete
		PageAction pa = new PageAction(false,true,true,false,false);
		uiModel.addAttribute(TwisterConstants.PAGE_ACTION, pa);    
		return "pgenrequests/todo/result";
	}

	/**
	 * getTodoSearchResultsDtoFromDataProcList function is called to set the TodoSearchListResultsDto from the DataProc result List
	 * 
	 * @param dataProcsList - List of DataProc results
	 * @return TodoSearchListResultsDto - List of TodoSearchResultsDto the result to be diaplayed
	 */
	private TodoSearchListResultsDto getTodoSearchResultsDtoFromDataProcList(List<DataProc> dataProcsList) {
		List<TodoSearchResultsDto> results = new ArrayList<TodoSearchResultsDto>();
		if(!CollectionUtils.isEmpty(dataProcsList)){
			for(DataProc dataProc : dataProcsList){
				TodoSearchResultsDto tmpRes = new TodoSearchResultsDto();
				tmpRes.setId(dataProc.getId());
				tmpRes.setReference(dataProc.getReference());
				tmpRes.setProcess(dataProc.getProcess());
				tmpRes.setOperReserver(dataProc.getOperReserver());
				tmpRes.setDteCreate(dataProc.getDteCreate());
				tmpRes.setStatus(dataProc.getStatus());
				tmpRes.setItsmNumber(dataProc.getItsmNumber());
				results.add(tmpRes);
			}
		}
		TodoSearchListResultsDto  searchResults = new TodoSearchListResultsDto();
		searchResults.setResults(results);
		return searchResults;
	}


	/*
	 * reserve Data Procs
	 */

	/**
	 * Reserve the dataProc from Todo lists
	 * 
	 * @param loggedUser - User connected to TWISTER 
	 * @param searchResults - TodoSearchListResultsDto to get the selected DataProcs to be reserved by the logged user
	 * @param searchDto - Search criteria
	 * @param searchPage - search page calling the function (todo, myrequest)
	 * @param page - number of pages 
	 * @param size - number of rows by page
	 * @param selectedLocale - Locale, the language selected
	 * @param uiModel - Model 
	 * @return search page with the reservation set
	 */
	@RequestMapping(value = "result", params="reserve", method = RequestMethod.POST, produces = TwisterConstants.PRODUCES_TEXT_HTML)
	public String reserveDataProcs(Principal loggedUser, @ModelAttribute("searchResults") TodoSearchListResultsDto searchResults, @ModelAttribute("searchDto") ToDoSearchRequest searchDto, @ModelAttribute(TwisterConstants.SEARCH_PAGE) String searchPage, 
			@RequestParam(value = TwisterConstants.PAGE, required = false) Integer page, @RequestParam(value = TwisterConstants.SIZE, required = false) Integer size, Locale selectedLocale, Model uiModel) {
		Operator curOperator = operatorService.findOperatorByLogin(loggedUser.getName());
		List<Long> resultsId = new ArrayList<Long>();
		List<Long> setNullReserverDataProcsId = new ArrayList<Long>();

		for(TodoSearchResultsDto item : searchResults.getResults()){
			//Selected object
			if(item.isSelected() && !curOperator.getCode().equalsIgnoreCase(item.getOperReserverIgg())){
				resultsId.add(item.getId());
			}
			else if(item.isSelected()){
				setNullReserverDataProcsId.add(item.getId());
			}
		}
		if(resultsId.size()!=0 || setNullReserverDataProcsId.size()!=0){
			dataProcService.setDataProcReservedBy(resultsId, curOperator, setNullReserverDataProcsId);
		}

		List<ProcessRefDto> processes = processRefService.findProcessesForOperatorAndCurrentProcess(curOperator.getCode(),PgenConstants.PGEN_SEARCH_PREFIX, selectedLocale);
		uiModel.addAttribute("listProcesses", processes);
		List<Entity> entities = entityService.findEntitiesByUserAndProcessId(curOperator, PgenConstants.PGEN_SEARCH_PREFIX);
		uiModel.addAttribute("listEntities", entities);
		uiModel.addAttribute("searchDto", searchDto);
		uiModel.addAttribute("searchResults", searchResults);
		if(!CollectionUtils.isEmpty(searchResults.getResults())){
			if(searchPage.equals(TwisterConstants.SEARCH_PAGE_TODO))
			{
				uiModel.addAttribute(TwisterConstants.SEARCH_PAGE, TwisterConstants.SEARCH_PAGE_TODO);
				uiModel.addAttribute("action", "resultTodo");
				PageAction pa = new PageAction(false,false,false,true,false);
				uiModel.addAttribute(TwisterConstants.PAGE_ACTION, pa);
			}
			else if(searchPage.equals(TwisterConstants.SEARCH_PAGE_MY_REQUEST))
			{
				uiModel.addAttribute(TwisterConstants.SEARCH_PAGE, TwisterConstants.SEARCH_PAGE_MY_REQUEST);
				uiModel.addAttribute("action", "resultMyRequest");
				PageAction pa = new PageAction(false,false,true,false,false);
				uiModel.addAttribute(TwisterConstants.PAGE_ACTION, pa);  
			}
			else if(searchPage.equals(TwisterConstants.SEARCH_PAGE_MY_DRAFT))
			{
				uiModel.addAttribute(TwisterConstants.SEARCH_PAGE, TwisterConstants.SEARCH_PAGE_MY_DRAFT);
				uiModel.addAttribute("action", "resultMyDrafts");
				PageAction pa = new PageAction(false,true,true,false,false);
				uiModel.addAttribute(TwisterConstants.PAGE_ACTION, pa); 
			}
		}
		addDateTimeFormatPatterns(uiModel,"S-");
		return "pgenrequests/todo/search";
	}



	/**
	 * Function called to Save the History of a DataProc in the table DATA_PROC_HISTORY in the database;
	 * 
	 * @param dataProc - DataProc
	 * @param details - history to be saved
	 */
	protected void persistHistory(DataProc dataProc, String details) {
		DataProcHistory newHistory = new DataProcHistory();
		newHistory.setDataProc(dataProc);
		newHistory.setDetails(details);
		newHistory.setDteLastAction(dataProc.getDteLastAction());
		//FIXME result.setLastAction(dataProc.getLastAction());
		newHistory.setOperator(dataProc.getOperLast());
		newHistory.setProcData(dataProc.getProcData());
		newHistory.setProcessTaskVersion(dataProc.getProcessTaskVersion());
		newHistory.setStatus(dataProc.getStatus());
		dataProcHistoryService.saveDataProcHistory(newHistory);
	}
	
	/**
	 * Function called to update the Comment of a DataProc in the table DATA_PROC_COMMENT in the database;
	 * 
	 * @param dataProc - DataProc 
	 * @param comment - the comment to be updated
	 */
	protected void updateComment(DataProc dataProc, String comment) {
		DataProcComment submittedComment = dataProcCommentService.findDraftCommentByDataProc(dataProc);
		if(submittedComment == null){
			submittedComment = new DataProcComment();
			submittedComment.initialize(dataProc, dataProc.getOperLast());
		}
		submittedComment.setText(comment);
		submittedComment.setDteComment(new Date());
		dataProcCommentService.updateDataProcComment(submittedComment);
	}

	   /**
	    * Function called to update Draft File;
	    * 
	 * @param data - DataProc
	 */
	protected void updateDraftFileToCreation(DataProc data){
	      if(data!=null && data.getFiles() != null){
	         if(data.getFiles().size() > 0){
	            for(DataProcFile file : data.getFiles()){
	               file.setProcessTaskVersion(data.getProcessTaskVersion());
	               file.setDteFileUp(data.getDteLastAction());
	            }
	         }
	      }
	   }

	/**
	 * persistComment function called to save the comments corresponding to DataProc in DATA_PROC_COMMENT table
	 * 
	* @param dataProc - DataProc 
	* @param pgenDto - PgenDto
	*/
	private void persistComment(DataProc dataProc, PgenDto pgenDto) {
	         DataProcComment submittedComment = new DataProcComment();
	         submittedComment.initialize(dataProc, dataProc.getOperLast());
	         submittedComment.setText(pgenDto.getComment());
	         dataProcCommentService.saveDataProcComment(submittedComment);
	   }
	   
	/** 
	 * persistDataProc function called to save or update the DataProc in DATA_PROC table
	 * 
	* @param dataProc - DataProc
	* @param pgenDto - PgenDto
	*/
	private void persistDataProc(DataProc dataProc, PgenDto pgenDto) {

	      //Creation
	      if(dataProc.getId() == null){
	         dataProc.setDteCreate(new Date());
	         dataProc.setDteLastAction(dataProc.getDteCreate());
	         dataProc.setOperLast(dataProc.getOperCreator());
	         //Save DataProc in DB
	           dataProcService.saveDataProc(dataProc);
	           dataProc.setReference(pgenDto.getHeader()+dataProc.getId());
	           logSecurity.debug("User ["+dataProc.getOperCreator().getLogin()+"] just created request with reference ["+dataProc.getReference()+"]");
	      }
	      else{ 
	           //Update
	           dataProc.setDteModify(new Date());
	           dataProc = dataProcService.updateDataProc(dataProc);
	      }
	   }
	
}
