package ar.com.fernandospr.wns.model;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "commands")
public class WnsCommand {

	@XmlAttribute
	public String id;

	@XmlAttribute
	public String arguments;
}
