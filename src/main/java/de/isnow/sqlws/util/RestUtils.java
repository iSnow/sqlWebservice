package de.isnow.sqlws.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.commons.codec.digest.DigestUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;



public class RestUtils {

	private RestUtils() {} // no instances, only static protocol

	/**
	 * Prepare a Response object that features eTag-support for
	 * one Object
	 *
	 * @returns Response
	 * @param obj
	 * @param request
	 * @param cc
	 * @param mapper
	 * @return
	 * @throws JsonProcessingException
	 */
	public static Response generateETagResponse(
			ETagHolder obj,
			Request request,
			CacheControl cc,
			ObjectMapper mapper) throws JsonProcessingException {
		
		EntityTag etag = new EntityTag(obj.getETag());
		ResponseBuilder responseBuilder = request.evaluatePreconditions(etag);
		
		if (responseBuilder == null) {
			// Etags don't match
			ObjectWriter w = mapper.writer();
			responseBuilder = Response.ok(w.writeValueAsString(obj)).tag(etag);
		}
		if (null != cc)
			responseBuilder.cacheControl(cc);
		responseBuilder.lastModified(new Date());
		
		return responseBuilder.build();
	}
	
	
	
	/**
	 * prepares a Response object that features eTag-support for
	 * a list of Objects
	 *
	 * @returns Response
	 * @param entities
	 * @param request
	 * @param granularity
	 * @param mapper
	 * @return
	 * @throws JsonProcessingException
	 */
	public static Response generateETagResponse(Collection<? extends ETagHolder> entities, 
			Request request,
			CacheControl cc, ObjectMapper mapper) throws JsonProcessingException {
		StringBuilder sb = new StringBuilder();
		for (ETagHolder eth : entities) {
			sb.append(eth.getETag());
		}
		EntityTag etag = new EntityTag(new String(DigestUtils.sha1Hex(sb.toString())));
		ResponseBuilder responseBuilder = request.evaluatePreconditions(etag);
		if (responseBuilder == null) {
			// Etags don't match
			ObjectWriter w = mapper.writer();
			responseBuilder = Response.ok(w.writeValueAsString(entities)).tag(etag);
		}
		if (null != cc) {
			responseBuilder.cacheControl(cc);
		}
		
		return responseBuilder.build();
	}
	
	public static Map<String, Object> createSearchResultJsonWrapper(Object obj) {
		Map<String, Object> responseNode = new LinkedHashMap<String, Object>();
		int size = 0;
		if (obj instanceof Collection) {
			responseNode.put("data", obj);
			size = ((Collection) obj).size();
		} else {
			responseNode.put("data", new Object[]{obj});
			size = 1;
		}
		
		responseNode.put("numFound", size);
		responseNode.put("maxScore", 0);
		responseNode.put("start", 0);
		responseNode.put("errors", 0);
		responseNode.put("warnings", 0);
		Map<String, Object> res = new LinkedHashMap<String, Object>();
		res.put("responseHeader", new LinkedHashMap<String, Object>());
		res.put("response", responseNode);
		return res;
	}

	public static Map<String, Object> createJsonWrapperForCollection(Object obj) {
		Map<String, Object> responseNode = new LinkedHashMap<String, Object>();
		int size = 0;
		if (obj instanceof Collection) {
			responseNode.put("data", obj);
			size = ((Collection) obj).size();
		} else {
			responseNode.put("data", new Object[]{obj});
			size = 1;
		}

		responseNode.put("model", "");
		responseNode.put("numFound", size);
		responseNode.put("errors", 0);
		responseNode.put("warnings", 0);
		return responseNode;
	}

	public static Map<String, Object> createJsonWrapperForSingleObject(Object obj) {
		Map<String, Object> responseNode = new LinkedHashMap<String, Object>();
		int size = 0;
		if (obj instanceof Collection) {
			responseNode.put("data", obj);
			size = ((Collection) obj).size();
		} else {
			responseNode.put("data", obj);
			size = 1;
		}

		responseNode.put("model", "");
		responseNode.put("numFound", size);
		responseNode.put("errors", 0);
		responseNode.put("warnings", 0);
		return responseNode;
	}
}
