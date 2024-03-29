package uk.ac.ncl.openlab.intake24.client.api.foods;

import com.google.gwt.core.client.GWT;
import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.Options;
import org.fusesource.restygwt.client.RestService;
import uk.ac.ncl.openlab.intake24.client.api.auth.AccessDispatcher;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import java.util.List;

@Options(dispatcher = AccessDispatcher.class, serviceRootKey = "intake24-api")
public interface FoodLookupService extends RestService {

    FoodLookupService INSTANCE = GWT.create(FoodLookupService.class);

    @GET
    @Path("/user/foods/{locale}/split-description")
    void getSplitSuggestion(@PathParam("locale") String localeId, @QueryParam("desc") String description, MethodCallback<SplitSuggestion> callback);

    @GET
    @Path("/user/foods/{locale}/lookup")
    void lookup(@PathParam("locale") String localeId, @QueryParam("alg") String algorithmId, @QueryParam("matchScoreWeight") int matchScoreWeight,
                @QueryParam("desc") String description, @QueryParam("existing") List<String> existingFoods,
                @QueryParam("limit") int limit, MethodCallback<LookupResult> callback);

    @GET
    @Path("/user/foods/{locale}/lookup-for-recipes")
    void lookupForRecipes(@PathParam("locale") String localeId, @QueryParam("alg") String algorithmId, @QueryParam("matchScoreWeight") int matchScoreWeight,
                          @QueryParam("desc") String description, @QueryParam("existing") List<String> existingFoods, @QueryParam("limit") int limit,
                          MethodCallback<LookupResult> callback);

    @GET
    @Path("/user/foods/{locale}/lookup-in-category")
    void lookupInCategory(@PathParam("locale") String localeId, @QueryParam("alg") String algorithmId, @QueryParam("matchScoreWeight") int matchScoreWeight,
                          @QueryParam("desc") String description, @QueryParam("category") String category, @QueryParam("existing") List<String> existingFoods,
                          @QueryParam("limit") int limit, MethodCallback<LookupResult> callback);
}
