package pro.belbix.tim.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import pro.belbix.tim.exchanges.Exchange;
import pro.belbix.tim.strategies.Strategy;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Service
public class AppContextService {
    private final ApplicationContext appContext;

    @Autowired
    public AppContextService(ApplicationContext appContext) {
        this.appContext = appContext;
    }

    public Set<Exchange> findExchanges(Set<String> exchangesToFind) {
        Set<Exchange> result = new HashSet<>();
        Map<String, Exchange> exchanges = appContext.getBeansOfType(Exchange.class);
        for (Exchange exchange : exchanges.values()) {
            if (exchangesToFind.contains(exchange.getExchangeName())) {
                result.add(exchange);
            }
        }
        return result;
    }

    public Exchange findExchange(String exchangeToFind) {
        Map<String, Exchange> exchanges = appContext.getBeansOfType(Exchange.class);
        for (Exchange exchange : exchanges.values()) {
            if (exchange.getExchangeName().equals(exchangeToFind)) {
                return exchange;
            }
        }
        return null;
    }

    public Strategy findStrategy(String strategyToFind) {
        Map<String, Strategy> strategies = appContext.getBeansOfType(Strategy.class);
        for (Strategy strategy : strategies.values()) {
            if (strategy.getStrategyName().equals(strategyToFind)) {
                return strategy;
            }
        }
        return null;
    }
}
