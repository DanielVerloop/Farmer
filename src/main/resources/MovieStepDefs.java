import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.And;
import org.junit.Assert;

public class MovieStepDefs {

    @Given("I want to setup a new movie")
    public void iWantToSetupANewMovie() {
        movie = new Movie();
    }

    Movie movie;

    @When("the movie has the {string} {string}")
    public void theMovieHasTheNameName(String name) {
        movie.setName(name);
    }

    @And("the movie has a {string} of {string}")
    public void theMovieHasARatingOfRating(String rating) {
        movie.setRating(rating);
    }

    @And("the {string} of the movie is {string}")
    public void theDurationOfTheMovieIsDuration(String duration) {
        movie.setDuration(duration);
    }

    @And("the cast of the movie is {string}")
    public void theCastOfTheMovieIsMainCast(String mainCast) {
        movie.setMainCast(mainCast);
    }

    @Then("I validate that the the new movie is setup correctly.")
    public void iValidateThatTheTheNewMovieIsSetupCorrectly.() {
        Assert.assertTrue(movie.getMovieId() == failed);
    }
}
