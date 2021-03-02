package org.yp.camel;

import org.apache.camel.LoggingLevel;
import org.apache.camel.Predicate;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.caffeine.CaffeineConstants;
import org.apache.camel.Exchange;
import static org.apache.camel.support.builder.PredicateBuilder.not;

/**
 * CamelIntegration
 */
public class CamelIntegration extends RouteBuilder {

    Predicate GotResult = header(CaffeineConstants.ACTION_HAS_RESULT).isEqualTo(true);
    @Override
    public void configure() throws Exception {
         from("timer:GetWeatherInfo?period=3600000").routeId("Get weather info")
            // Setting up constants
            .setHeader("CITY_LIST_CACHE_KEY", constant("CITY_LIST_CACHE_KEY"))
            .setHeader("CITY_LIST_CACHE", constant("CITY_LIST_CACHE"))
            .setHeader("CITY_CACHE_KEY_PREFIX", constant("CITY_CACHE_KEY_PREFIX_"))
            .setHeader("CITY_CACHE", constant("CITY_CACHE"))
            .log(LoggingLevel.INFO, ">".repeat(6) + "Getting City list " + ">".repeat(6))
            .to("direct:getCityList")
            .setHeader("ProvinceToGet", constant("BC"))
            .process(new XmlParser())
            .split(body()).stopOnException().parallelProcessing()
                .to("seda:processCity?concurrentConsumers=2")
                // .choice()
                //     .when(simple("${header.CamelSplitIndex} > 10"))
                //         .throwException(new Exception("Enough"))
                //     .end()
                // .end()
            .end()
        .end();

        from("seda:processCity").routeId("Process city weather")
            .process(new Processor(){
                public void process(Exchange exchange) {
                    Site s = exchange.getIn().getBody(Site.class);
                    exchange.getIn().setHeader("CityCode", s.getCode());
                    exchange.getIn().setHeader("CityProvinceCode", s.getProvinceCode());
                    exchange.getIn().setHeader("CityNameEn", s.getNameEn());
                }
            })
            .log(LoggingLevel.INFO, ">".repeat(3) + "Getting Weather information for ${header.CityNameEn} [${header.CityCode}] " + ">".repeat(3))
            .to("direct:getBcCityInfo")
            .to("direct:processCityFile")
            .log(LoggingLevel.INFO, "<".repeat(3) + "Information for ${header.CityNameEn} [${header.CityCode}] retrieved " + "<".repeat(3))
        .end();

        from("direct:getCityList").routeId("Get city list")
            .setHeader(CaffeineConstants.ACTION, constant(CaffeineConstants.ACTION_GET))
            .setHeader(CaffeineConstants.KEY, simple("${header.CITY_LIST_CACHE_KEY}"))        
            .toD("caffeine-cache://${header.CITY_LIST_CACHE}")
            .choice()
                .when(not(GotResult))
                    .log(LoggingLevel.INFO, "Retrieving city list from Weather Canada")
                    .to("direct:getCityListFromHTTP")
                    .setHeader(CaffeineConstants.ACTION, constant(CaffeineConstants.ACTION_PUT))
                    .toD("caffeine-cache://${header.CITY_LIST_CACHE}")
                .otherwise()
                    .log(LoggingLevel.INFO, "City list retrieved from cache")
            .end()
        .end();

    from("direct:getCityListFromHTTP").routeId("Getting city list from HTTP")
        .setHeader("Accept", constant("*/*"))
        .setHeader("User-Agent", constant("ApacheCamel"))
        .setHeader("Connection", constant("keep-alive"))
        .setHeader(Exchange.HTTP_METHOD, constant("GET"))
        .to("https://dd.weather.gc.ca/citypage_weather/xml/siteList.xml")
        .convertBodyTo(String.class)
    .end();

    from("direct:getBcCityInfo").routeId("Getting weather info for bc city")
        .throttle(1).timePeriodMillis(1000)
        .setHeader(Exchange.HTTP_METHOD, constant("GET"))
        .toD("https://dd.weather.gc.ca/citypage_weather/xml/${header.CityProvinceCode}/${header.CityCode}_e.xml")
        .convertBodyTo(String.class)
        .process(new ChecksumProcessor())
        .log(LoggingLevel.INFO, "Weather information for ${header.CityNameEn} [${header.CityCode}] retrieved from Weather Canada")
    .end();

    from("direct:processCityFile").routeId("Parsing weather file")
        .multicast().aggregationStrategy(new ChecksumAggregationStrategy())
            .to("seda:waitForCheckSum?waitForTaskToComplete=Always", "seda:checkCache?waitForTaskToComplete=Always")
        .end()
        .choice()
            .when(simple("${header.FileExists} == true"))
                .log(LoggingLevel.INFO, "No new data available for ${header.CityNameEn} [${header.CityCode}]")
            .otherwise()
                .setHeader("CamelFileName", simple("${header.CityCode}-${header.CityNameEn}/${date:now:yyyy-MM-dd_HHmmssSSS}.${header[CheckSum]}.xml"))
                .log("Folder: ${properties:application.destination:/res}")
                .toD("file://${properties:application.destination:/res}")
                .log(LoggingLevel.INFO, "Weather data for ${header.CityNameEn} [${header.CityCode}] was saved to ${header.CamelFileName}")
        .end()
    .end();

    from("seda:checkCache").routeId("Check city weather cache").setBody(simple(""))
        .setHeader(CaffeineConstants.ACTION, constant(CaffeineConstants.ACTION_GET))
        .setHeader(CaffeineConstants.KEY, simple("${header.CITY_CACHE_KEY_PREFIX}${header.CheckSum}"))
        .toD("caffeine-cache://${header.CITY_CACHE}")
        .setHeader("FileExists", simple("${header.CamelCaffeineActionHasResult} == true && ${header.CheckSum} == ${body}", boolean.class))
        .setHeader(CaffeineConstants.ACTION, constant(CaffeineConstants.ACTION_PUT))
        .setHeader(CaffeineConstants.VALUE, simple("${header[CheckSum]}"))
        .toD("caffeine-cache://${header.CITY_CACHE}")
        .log("Cache check complete result: ${header.FileExists}")

    .end();

    from("seda:waitForCheckSum").routeId("Waiting for a duplicate checkup")
        .log("Waiting for a file cache record check.")
    .end();
    }
}