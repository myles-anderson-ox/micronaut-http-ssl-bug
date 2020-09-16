
package example;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.validation.Validated;

@Validated
@Controller("/")
public class MyApi {

    @Get
    public String get() {
        return "ok";
    }
}
