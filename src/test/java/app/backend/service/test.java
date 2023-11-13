package app.backend.service;

import app.backend.document.light.TrafficLight;
import app.backend.document.light.TrafficLightType;
import app.backend.request.optimization.OptimizationRequest;
import app.backend.response.optimization.OptimizationResultResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.junit.jupiter.api.Test;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class test {

    @Test
    public void deleteCadrFlowById_improperCarFlow_carFlowNotFound() throws JsonProcessingException {
        HashMap<Integer, Double> map = new HashMap<>();
        map.put(123, 123.41);
        map.put(4, 13.41);
        map.put(14, 12.);

        HashMap<Integer, List<Integer>> mapint = new HashMap<>();
        ArrayList<Integer> listint = new ArrayList<>();
        listint.add(123);
        listint.add(13);
        listint.add(12);
        mapint.put(12, listint);

        HashMap<Integer, List<TrafficLight>> maplight = new HashMap<>();
        ArrayList<TrafficLight> listdir = new ArrayList<>();
        listdir.add(new TrafficLight(1, TrafficLightType.FORWARD));
        listdir.add(new TrafficLight(2, TrafficLightType.LEFT));
        listdir.add(new TrafficLight(3, TrafficLightType.UTURN));
        maplight.put(13, listdir);

        HashMap<Integer, List<TrafficLight>> mapcon = new HashMap<>();
        listdir = new ArrayList<>();
        listdir.add(new TrafficLight(1, TrafficLightType.FORWARD));
        listdir.add(new TrafficLight(2, TrafficLightType.LEFT));
        listdir.add(new TrafficLight(3, TrafficLightType.UTURN));
        maplight.put(13, listdir);

        HashMap<Integer, TrafficLightType> mapdir = new HashMap<>();
        mapdir.put(142, TrafficLightType.LEFT);
        mapdir.put(12, TrafficLightType.FORWARD);
        mapdir.put(1452, TrafficLightType.UTURN);

        OptimizationResultResponse response = new OptimizationResultResponse(mapint, map, null, null, maplight, mapcon, mapdir);

        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String json = ow.writeValueAsString(response);

//        JSONObject json = new JSONObject(response);
        System.out.println(json);
    }

    @Test
    public void orderOptimization() throws JsonProcessingException {
        OptimizationRequest optimizationRequest = new OptimizationRequest();

        String url = "http://localhost:9091/optimization";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<OptimizationRequest> requestEntity = new HttpEntity<>(optimizationRequest, headers);
        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<String> response;

        response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);


        System.out.println(response.getBody());

        HashMap<Integer, Object> mapping = new ObjectMapper().readValue(response.getBody(), HashMap.class);

        List<List<Integer>> sequences = new ArrayList<>(mapping.size());
        for(int i=0;i<mapping.size();i++){
            String sequenceAsString = mapping.get(Integer.valueOf(i+1)).toString();
            List<Integer> list = new ArrayList<>();
            for(int idx=1;idx<sequenceAsString.length();idx+=3){
                list.add(Integer.parseInt(String.valueOf(sequenceAsString.charAt(idx))));
            }

            sequences.add(list);
        }
        System.out.println(sequences);

    }
}
