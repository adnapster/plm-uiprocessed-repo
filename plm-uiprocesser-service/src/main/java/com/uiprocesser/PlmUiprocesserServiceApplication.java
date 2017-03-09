package com.uiprocesser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import com.netflix.discovery.EurekaClient;
import com.uiprocesser.domain.SymixData;
import com.uiprocesser.service.PLMUiprocessService;

@SpringBootApplication
@RestController
@EnableEurekaClient
public class PlmUiprocesserServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(PlmUiprocesserServiceApplication.class, args);
	}
	
	private static final Logger LOG = LoggerFactory.getLogger(PlmUiprocesserServiceApplication.class);

	@Bean
	RestTemplate restTemplate() {
		return new RestTemplate();
	}

	@Autowired
	RestTemplate restTemplate;
	@Autowired
	EurekaClient eurekaClient;
	
	@Autowired
	PLMUiprocessService plmUiProcessService;
	
	@Autowired
	private DiscoveryClient discoveryClient;
	@Value("${apigatewayms.name}")
	private String apigatewaymsName;
	
	@Value("${plmpayload.reprocess.resource}")
	private String plmpayloadprocessmsResource;
	@Value("${plmpayload.reprocess.mapping}")
	private String plmpayloadprocessmsMapping;
	@Value("${map.xml.key}")
	private String xml;
	@Value("${map.ecnno.key}")
	private String ecnno;

	@Value("${map.transactionid.key}")
	private String transactionid;

	@Value("${map.plant.key}")
	private String plant;

	@Value("${map.description.key}")
	private String description;

	@Value("${map.type.key}")
	private String type;

	@Value("${map.createdby.key}")
	private String createdby;

	@Value("${map.createddate.key}")
	private String createddate;
	@Value("${map.uiProcessedBy.key}")
	private String uiProcessedBy;

	@Value("${map.uiProcesseddate.key}")
	private String uiprocesseddate;

	@Value("${map.uiProcessedcomment.key}")
	private String uiProcessedcomment;
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@RequestMapping(value = "/reprocessWebportalXML", method = RequestMethod.POST)
	@ResponseBody
	public boolean reprocessWebportalXML(HttpServletRequest request) {
		if (LOG.isDebugEnabled()) {
			LOG.info("###### Starting PlmUiprocesserServiceApplication.reprocessWebportalXML() #######");
		}
		
		System.out.println("################ I am Called###################")
		List<ServiceInstance> apigatewaymsInstanceList = discoveryClient.getInstances(apigatewaymsName);
		ServiceInstance apigatewaymsInstance = apigatewaymsInstanceList.get(0);
		boolean isXMLReprocessed = false;
		ObjectMapper mapper = new ObjectMapper();
		try {
			mapper.configure(org.codehaus.jackson.map.DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			SymixData symixData = mapper.readValue(request.getParameter("data"), SymixData.class);
			for (int i = 0; i < symixData.getSeries().size(); i++) {
				String xmlData = plmUiProcessService.readBlobXML(symixData.getSeries().get(i).getECNNumber());
				Map<String, String> reprocessingMap = new HashMap<String, String>();
				reprocessingMap.put(xml, xmlData);
				reprocessingMap.put(ecnno, symixData.getSeries().get(i).getECNNumber());
				reprocessingMap.put(transactionid, symixData.getSeries().get(i).getTransactionID());
				reprocessingMap.put(plant, symixData.getSeries().get(i).getPlant());
				reprocessingMap.put(description, symixData.getSeries().get(i).getECNDescription());
				reprocessingMap.put(type, symixData.getSeries().get(i).getECNType());
				reprocessingMap.put(createdby, symixData.getSeries().get(i).getCreatedBy());
				reprocessingMap.put(createddate, symixData.getSeries().get(i).getCreatedDate());
				reprocessingMap.put(uiProcessedBy, symixData.getSeries().get(i).getUIProcessedBy());
				reprocessingMap.put(uiprocesseddate, symixData.getSeries().get(i).getUIProcessedDate());
				reprocessingMap.put(uiProcessedcomment, symixData.getSeries().get(i).getUIProcessingComments());
				HttpEntity entity = new HttpEntity(reprocessingMap, new HttpHeaders());
				if (LOG.isDebugEnabled()) {
					LOG.info("For reprocessing we are going to hit");
					LOG.info("####################################################################################");
					LOG.info("We are going to Hit " + apigatewaymsInstance.getUri().toString() + plmpayloadprocessmsResource+plmpayloadprocessmsMapping);
					LOG.info("=================" + symixData.getSeries().get(i).getUIProcessedBy());
					LOG.info("=================" + symixData.getSeries().get(i).getUIProcessedDate());
					LOG.info("=================" + symixData.getSeries().get(i).getUIProcessingComments());
				}
				/*restTemplate.exchange(
						"http://" + loadBalancedInstance.getAppName().toLowerCase() + ":"
								+ Integer.toString(loadBalancedInstance.getPort()) + plmpayloadprocessmsMapping,
						HttpMethod.POST, entity, String.class);*/
				
				restTemplate.exchange(
						apigatewaymsInstance.getUri().toString() + plmpayloadprocessmsResource+plmpayloadprocessmsMapping, HttpMethod.POST,
						entity, String.class);

			}
			isXMLReprocessed = true;
		} catch (Exception e) {
			LOG.error("###### Generic Exception is occured in PlmUiprocesserServiceApplication.reprocessWebportalXML() #######",
					e);
		}
		return isXMLReprocessed;
	}
}
