package domain;

import java.io.Serializable;
import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.context.SecurityContextHolder;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class MyrequestDto implements Serializable {
   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   public MyrequestDto(){
      if(StringUtils.isBlank(this.requestId)){
         String loggedUser = SecurityContextHolder.getContext().getAuthentication().getName();
         if(!StringUtils.isBlank(loggedUser)){
            this.requestId = getNewIdRequest(loggedUser);
         }
      }
   }
   
   public MyrequestDto(String operCreator){
      this.setRequestId(getNewIdRequest(operCreator));
   }
   
   private String getNewIdRequest(String operCreator) {
      StringBuilder uniqueId = new StringBuilder();
      uniqueId.append(String.valueOf(new Date().getTime())).append("-").append(operCreator);
      return DigestUtils.md5Hex(uniqueId.toString());      
   }
   
   
   @XmlTransient
//    @Size(max=5000)
    private String comment;
    
    @XmlTransient
    private String header;
    
    
    @XmlTransient
    private String requestId;

	
	public String getComment() {
        return this.comment;
    }

	public void setComment(String comment) {
        this.comment = comment;
    }

	public String getHeader() {
        return this.header;
    }

	public void setHeader(String header) {
        this.header = header;
    }

	
	public String getRequestId() {
        return this.requestId;
    }

	public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
}

