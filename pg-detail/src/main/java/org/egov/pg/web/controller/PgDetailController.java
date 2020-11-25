package org.egov.pg.web.controller;

import javax.validation.Valid;

import org.egov.pg.service.PgDetailService;
import org.egov.pg.web.contract.PgDetailRequest;
import org.egov.pg.web.contract.PgDetailResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
 

@Controller 
public class PgDetailController {

	 

	@Autowired
	private PgDetailService service;

 

	@Autowired
	private org.egov.pg.utils.ResponseInfoFactory factory;
	/**
	 * Creates Payment Gateway details for cantonment board
	 * @param pgDetailRequest
	 * @return
	 */
	@RequestMapping(value = "/_create", method = RequestMethod.POST)
	public ResponseEntity<PgDetailResponse> createPgDetails(@Valid @RequestBody PgDetailRequest pgDetailRequest) {
		PgDetailResponse response = service.createPgDetails(pgDetailRequest);
		return new ResponseEntity<>(response, HttpStatus.CREATED);
	}
	
	@RequestMapping(value = "/_get", method = RequestMethod.POST)
	public ResponseEntity<PgDetailResponse> getPgDetails(@RequestBody PgDetailRequest pgDetailRequest){
		PgDetailResponse response = service.getPgDetails(pgDetailRequest);
		return new ResponseEntity<>(response, HttpStatus.OK);
		
	}
	@RequestMapping(value = "/_update", method = RequestMethod.POST)
	public ResponseEntity<PgDetailResponse> updatePgDetails(@RequestBody PgDetailRequest pgDetailRequest){
		PgDetailResponse response = service.updatePgDetails(pgDetailRequest);
		return new ResponseEntity<>(response, HttpStatus.OK);
		
	}
	 
}
