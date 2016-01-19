package ar.com.fernandospr.wns.model;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "commands")
public class WnsCommands {

	@XmlAttribute
	public String scenario;

	@XmlElement(name = "command")
	public List<WnsCommand> commands;
}
