/*
 * Copyright (c) 2003, Orientation in Objects GmbH, www.oio.de
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 * 
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation 
 *   and/or other materials provided with the distribution.
 * - Neither the name of Orienation in Objects GmbH nor the names of its 
 *   contributors may be used to endorse or promote products derived from this 
 *   software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES 
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY 
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, 
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * ${file_name} Created on ${date} by ${user}
 *  
 * ${todo}
 * 
 */

package de.isnow.sqlws.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import de.isnow.sqlws.resources.ColumnModelService;
import lombok.Data;
import lombok.Getter;
import org.glassfish.jersey.linking.Binding;
import org.glassfish.jersey.linking.InjectLink;
import org.glassfish.jersey.linking.InjectLinks;
import schemacrawler.schema.Column;
import schemacrawler.schema.ColumnDataType;

import javax.validation.constraints.NotNull;
import javax.ws.rs.core.Link;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author tbayer
 *
 */

@JsonAutoDetect(
		fieldVisibility = JsonAutoDetect.Visibility.ANY,
		getterVisibility = JsonAutoDetect.Visibility.NONE,
		setterVisibility = JsonAutoDetect.Visibility.NONE)
@Data
public class WsColumn extends WsObject {

	@JsonIgnore
	private Column column;

	/* normalized (by Schemacrawler) SQL data types:
		binary
		bit
		character
		id
		integer
		large_object
		object
		real
		reference
		temporal
		unknown
		url
		xml
	 */
	@Getter
	private String dataType;

	@Getter
	private int type, size;

	@Getter
	private boolean primaryKey, foreignKey;

	private Set<String> referencedBy = new TreeSet<>();
	
	public WsColumn() {
		register(this);
	}

	public WsColumn(Column c) {
		super(c);
		register(this);
		this.column = c;
		this.name = c.getName();
		this.fullName = c.getFullName();
		ColumnDataType type = c.getColumnDataType();
		dataType = type.getJavaSqlType().getJavaSqlTypeGroup().name();
		primaryKey = c.isPartOfPrimaryKey();
		foreignKey = c.isPartOfForeignKey();
		size = c.getSize();
	}
	
	public WsColumn(String nameIn, int typeIn, int sizeIn) {
		this();
		this.name = nameIn;
		this.type = typeIn;
		this.size = sizeIn;
	}

	public static WsColumn get(@NotNull String id) {
		return (WsColumn)registries
				.get(WsColumn.class)
				.get(id);
	}

	public void addReferencedBy(WsColumn other) {
		referencedBy.add(other.fullName);
	}

	public void addReferencedBy(Collection<WsColumn> others) {
		others.forEach((c) -> referencedBy.add(c.fullName));
	}

	@InjectLinks({
			@InjectLink(
					resource= ColumnModelService.class,
					method="getColumn",
					bindings ={
							@Binding(name = "id",
									value = "${instance.id}")
					},
					rel = "self",
					title = "this Column",
					type = "GET",
					style =  InjectLink.Style.RELATIVE_PATH
			)/*,
			@InjectLink(
					resource=ColumnService.class,
					method="getColumns",
					bindings ={
							@Binding(name = "tableid",
									value = "${instance.id}")
					},
					rel = "self",
					title = "tables",
					type = "GET",
					style =  InjectLink.Style.RELATIVE_PATH
			)*/
	})
	List<Link> links;
}
