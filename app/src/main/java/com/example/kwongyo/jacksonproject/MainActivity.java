package com.example.kwongyo.jacksonproject;

import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import org.codehaus.jackson.map.DeserializationConfig;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

import retrofit2.Call;
import retrofit2.Callback;

import retrofit2.Response;
import retrofit2.Retrofit;

import retrofit2.converter.jackson.JacksonConverterFactory;
import retrofit2.http.GET;

import retrofit2.http.Path;

public class MainActivity extends AppCompatActivity {
    public static final String BASEURL="https://api.github.com/";
    @Bind(R.id.sendBtn)
    Button sendBtn;
    @Bind(R.id.textView)
    TextView textView;
    Retrofit retrofit;

    Call<List<Contributor>> call;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        ObjectMapper objectMapper = new ObjectMapper();

        final JacksonConverterFactory jacksonConverterFactory = JacksonConverterFactory.create(objectMapper);



/*
* Json Mapper 를 이용해 Java Bean 에 데이터를 넣을때
JSON에서 넘겨 받는 필드가
Bean 에 없으면 JsonMappingException: Unrecognized field 요런 에러가 뜬다.
http://blog.naver.com/PostView.nhn?blogId=sungwooks&logNo=20139147346
*/

        retrofit = new Retrofit.Builder().baseUrl(BASEURL).addConverterFactory(JacksonConverterFactory.create()).build();
        GitHubRequest gitHubRequest = retrofit.create(GitHubRequest.class);
        call = gitHubRequest.repoContributors("square","retrofit");
    }
    @OnClick(R.id.sendBtn)
    public void sendBtnClick ( View view ) {
        call . clone() . enqueue(new Callback<List<Contributor>>() {
            @Override
            public void onFailure(Call<List<Contributor>> call, Throwable t) {
                textView.setText(t.getMessage()+"--false--");
                Toast.makeText(getApplicationContext(),t.getMessage(),Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(Call<List<Contributor>> call, Response<List<Contributor>> response) {
                Toast.makeText(getApplicationContext(),response.isSuccessful()+"",Toast.LENGTH_SHORT).show();
                if (!response.isSuccessful()) {
                    textView.setText(response.message() + "///////////" + response.headers());
                    return;
                }

                List<Contributor> contributorList = response.body();

                textView.setText(response.body().toString());



                StringBuilder builder = new StringBuilder();
                for (Contributor contributor : contributorList) {
                    builder.append(contributor.toString()+"\n");
                }
                textView.setText(builder.toString());


            }
        });
    }
    interface GitHubRequest{
        // Call 객체는 요청/응답이 한 쌍으로써 요청을 보낼 때 execute을 호출하지 않았음을 보장해야 한다.
        @GET("/repos/{owner}/{repo}/contributors")
        Call<List<Contributor>> repoContributors(
                @Path("owner") String owner,
                @Path("repo") String repo
        );
    }

    /**
     * JsonIgnoreProperties가 없으면 직렬화가 안댐..ㅠ
     */
    @JsonIgnoreProperties(ignoreUnknown = true) /*이게 없으면 직렬화가 안된단다.*/
    static class Contributor {
        private String login;
        private int id;

        public Contributor() {
        }

        public Contributor(int id, String login) {this.id = id;this.login = login;}
        public String getLogin() {return login;}
        public void setLogin(String login) {this.login = login;}
        public int getId() {return id;}
        public void setId(int id) {this.id = id;}
        @Override
        public String toString() {return "login='" + login + '\'' +", id=" + id;}
    }





}
