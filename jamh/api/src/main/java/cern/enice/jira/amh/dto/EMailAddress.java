package cern.enice.jira.amh.dto;

import java.util.Objects;

public class EMailAddress {
	private String personalName;
	private String localPart;
	private String domain;
	
	public String getPersonalName() {
		return personalName;
	}
	public void setPersonalName(String personalName) {
		this.personalName = personalName;
	}
	public String getLocalPart() {
		return localPart;
	}
	public void setLocalPart(String localPart) {
		this.localPart = localPart;
	}
	public String getDomain() {
		return domain;
	}
	public void setDomain(String domain) {
		this.domain = domain;
	}
	
	public EMailAddress(String personalName, String localPart, String domain) {
		super();
		this.personalName = personalName;
		this.localPart = localPart;
		this.domain = domain;
	}
	
	@Override
	public String toString() {
		if (localPart == null || domain == null) return "";
		return personalName == null ? localPart + "@" + domain : String.format("%s <%s@%s>", personalName, localPart, domain);
	}
	
	@Override
	public boolean equals(Object o) {
		if(o == null) return false;
	    if(!(o instanceof EMailAddress)) return false;
	    EMailAddress other = (EMailAddress) o;
	    return this.toString().equals(other.toString());
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(this.personalName, this.localPart, this.domain);
	}
	
}
