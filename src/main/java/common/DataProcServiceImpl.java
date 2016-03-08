package common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import repository.DataProcRepository;
import model.DataProc;

public class DataProcServiceImpl implements DataProcService{

	@Autowired 
	private	 DataProcRepository dataProcRepository;
	
	@Override
	@Transactional( isolation = Isolation.DEFAULT, propagation=Propagation.SUPPORTS, readOnly=true)
	public DataProc findDataProc(Long id) {
		// TODO Auto-generated method stub
		return dataProcRepository.findOne(id);
	}

}
