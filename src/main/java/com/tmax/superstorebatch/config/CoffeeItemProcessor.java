package com.tmax.superstorebatch.config;

import com.tmax.superstorebatch.Coffee;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

import java.util.logging.Logger;

@Slf4j
public class CoffeeItemProcessor implements ItemProcessor<Coffee, Coffee> {

    @Override
    public Coffee process(final Coffee coffee) throws Exception {
        String brand = coffee.getBrand().toUpperCase();
        String origin = coffee.getOrigin().toUpperCase();
        String chracteristics = coffee.getCharacteristics().toUpperCase();

        Coffee transformedCoffee = new Coffee(brand, origin, chracteristics);
        log.info("Converting ( {} ) into ( {} )", coffee, transformedCoffee);

        return transformedCoffee;
    }


}
