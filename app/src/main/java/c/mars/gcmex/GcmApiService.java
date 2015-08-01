package c.mars.gcmex;

import lombok.AllArgsConstructor;
import lombok.Data;
import retrofit.http.Body;
import retrofit.http.Headers;
import retrofit.http.POST;
import rx.Observable;

/**
 * Created by Constantine Mars on 8/1/15.
 */
public interface GcmApiService {
    @Headers({
            "Content-Type: application/json",
            "Authorization: key=AIzaSyBJSnRxoEBEHnvg2wkpssOEkepS0Aloz2Y"
    })
    @POST("/gcm/send")
    Observable<MessageId> send(@Body Message message);

    @Data
    class MessageId {
        String message_id;
    }

    @Data
    class Message{
        String to;
        DataStruct data;

        public Message(String to, String message) {
            this.to = to;
            this.data = new DataStruct(message);
        }

        @Data @AllArgsConstructor
        public class DataStruct{
            String message;
        }
    }
}
