package org.egov.pg.web.controllers;
 
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.util.UriComponentsBuilder;

import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class RedirectController {

    @Value("${egov.default.citizen.url}")
    private String defaultURL;

    @Value("${nic.original.return.url.key:originalreturnurl}")
    private String returnUrlKey;
    

    @RequestMapping(value = "/transaction/v1/_redirect", method = RequestMethod.POST, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<Object> method(@RequestBody MultiValueMap<String, String> formData ,HttpServletRequest request  ) throws URISyntaxException, MalformedURLException {
    	StringBuffer callerUrl = request.getRequestURL();
    	String baseUrl =callerUrl.substring(0 ,callerUrl.indexOf(request.getContextPath()));
    	log.info("base url "+baseUrl);
    	HttpHeaders httpHeaders = new HttpHeaders();
        String redirectUrl =baseUrl + formData.get(returnUrlKey).get(0);
        httpHeaders.setLocation(UriComponentsBuilder.fromHttpUrl(redirectUrl)
                .build().encode().toUri());
        log.error(httpHeaders!=null ? httpHeaders.toString(): "http header is null ");
        log.info("http location "+ httpHeaders.getLocation());
        
        return new ResponseEntity<>(httpHeaders, HttpStatus.FOUND);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleError(Exception e) {
        log.error("EXCEPTION_WHILE_REDIRECTING", e);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setLocation(UriComponentsBuilder.fromHttpUrl(defaultURL).build().encode().toUri());
        return new ResponseEntity<>(httpHeaders, HttpStatus.FOUND);
    }

}
