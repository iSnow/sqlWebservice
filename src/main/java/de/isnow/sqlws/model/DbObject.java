package de.isnow.sqlws.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.Setter;

public class DbObject {
	@Getter
	@Setter
	@JsonIgnore
	private DBConnection owningConnection;

}
