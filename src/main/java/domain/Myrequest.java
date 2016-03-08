package domain;

import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;


@XmlRootElement
@XmlType(propOrder={"requestType", "priority"})
@XmlAccessorType(XmlAccessType.FIELD)
public class Myrequest extends MyrequestDto{
   private static final long serialVersionUID = 1L;
   
   public Myrequest(){
      this.setHeader("REQ-");
   }

   public Myrequest(String operCreator) {
      super(operCreator);
      this.setHeader("REQ-");
   }
   
   @NotNull(message="{pgen.requestType.NotNull}")
   private String requestType;
   
   @NotNull(message="{pgen.priority.NotNull}")
   private String priority;
   

	public String getRequestType() {
        return this.requestType;
    }

	public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

	public String getPriority() {
        return this.priority;
    }

	public void setPriority(String priority) {
        this.priority = priority;
    }

	public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
