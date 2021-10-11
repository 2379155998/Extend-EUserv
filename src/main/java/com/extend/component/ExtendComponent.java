package com.extend.component;

import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.net.HttpCookie;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ExtendComponent {
    @EventListener(ContextRefreshedEvent.class)
    public void extend() {
        Map<String,String> account = new HashMap<>();
        account.put("keiko.nishimata@yandex.com","345N.3M8ax5nfDK");
        account.put("nori.tsukiji@yandex.com","ysyhl9_T");
        account.put("hashiura@yandex.com","Toshiyuki342");
        account.put("kind.koji@yandex.com","hRfWxEeNKs9gY!7");
        account.put("atsukooshima@yandex.com","rsbBtqgW.gB69re");

        account.forEach((email, password) -> {
            HttpResponse response = login(email,password);
            List<String> contractIDList = getContractID(response);
            extend(contractIDList,response,password);
        });
    }

    public void extend(List<String> contractIDList,HttpResponse response,String password) {
        String PHPSESSID = response.getCookieValue("PHPSESSID");
        String url = "https://support.euserv.com/index.iphp";

        for (String contractID : contractIDList) {
            HttpResponse httpResponse = HttpUtil.createPost(url)
                    .form("Submit", "Extend contract")
                    .form("sess_id", PHPSESSID)
                    .form("ord_no", contractID)
                    .form("subaction", "choose_order")
                    .form("choose_order_subaction", "show_contract_details").execute();

            HttpResponse execute = HttpUtil.createPost(url)
                    .form("sess_id", PHPSESSID)
                    .form("subaction", "kc2_security_password_get_token")
                    .form("prefix", "kc2_customer_contract_details_extend_contract_")
                    .form("password", password).execute();
            if (execute.getStatus()==200){
                System.out.println(execute.body());

                JSONObject jsonObject = JSONUtil.parseObj(execute.body());
                JSONObject tokenJSONObject = jsonObject.getJSONObject("token");


                if ("success".equals(jsonObject.getStr("rs"))) {
                    String token = tokenJSONObject.getStr("value");

                    HttpResponse execute1 = HttpUtil.createPost(url)
                            .form("sess_id", PHPSESSID)
                            .form("ord_id", contractID)
                            .form("subaction", "kc2_customer_contract_details_extend_contract_term")
                            .form("token", token).execute();
                }
            }
        }
    }

    public List<String> getContractID(HttpResponse response) {
        Document document = Jsoup.parse(response.body());
        Elements trs = document.select("#kc2_order_customer_orders_tab_content_1 .kc2_order_table.kc2_content_table");

        List<String> contractIDList = new ArrayList<>();

        trs.forEach(tr -> {
            // 合同ID
            Elements contractIDElement = tr.select(".td-z1-sp1-kc");
            String contractID = contractIDElement.html();
            contractIDList.add(contractID);

            // 合同延期时间
            Elements contractExtensionElements = tr.select(".kc2_order_extend_contract_term_container");
            String contractExtension = contractExtensionElements.html();
        });
        return contractIDList;
    }

    public HttpResponse login(String email, String password) {
        HttpResponse response = HttpUtil.createGet("https://support.euserv.com/")
                .execute();
        String PHPSESSID = response.getCookieValue("PHPSESSID");

        HttpUtil.get("https://support.euserv.com/pic/logo_small.png");

        return HttpUtil.createPost("https://support.euserv.com/index.iphp")
                .cookie(new HttpCookie("PHPSESSID",PHPSESSID))
                .form("email", email)
                .form("password", password)
                .form("form_selected_language", "en")
                .form("Submit", "Login")
                .form("subaction", "login")
                .form("sess_id", PHPSESSID)
                .execute();
    }
}
