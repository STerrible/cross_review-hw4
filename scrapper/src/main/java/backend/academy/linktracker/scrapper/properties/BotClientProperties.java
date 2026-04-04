package backend.academy.linktracker.scrapper.properties;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.URL;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.bot")
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
public class BotClientProperties {

    @URL
    private String baseUrl;
}
