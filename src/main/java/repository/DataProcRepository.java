package repository;

import model.DataProc;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface DataProcRepository extends JpaRepository<DataProc, Long>, JpaSpecificationExecutor<DataProc>{

	

}
