package com.dev.rebook.controllers;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

@Controller
@RequestMapping(value = "/test")
public class TestController {
    @RequestMapping(value = "/", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public String getTest() {
        return "test/test";
    }

    @RequestMapping(value = "/api/aladin", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<String> searchBook(@RequestParam(value = "keyword") String keyword) {
        String query = keyword.replace(" ", "+");
        String url = "https://www.aladin.co.kr/ttb/api/ItemSearch.aspx"
                + "?ttbkey=ttbrkw21150008001"
                + "&Query=" + query
                + "&QueryType=Title"
                + "&MaxResults=1"
                + "&SearchTarget=Book"
                + "&output=XML"
                + "&Cover=Big";
        RestTemplate restTemplate = new RestTemplate();
        String response = restTemplate.getForObject(url, String.class);
        return ResponseEntity.ok(response);
    }

}
