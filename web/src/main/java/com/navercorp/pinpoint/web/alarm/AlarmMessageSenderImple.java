package com.navercorp.pinpoint.web.alarm;

import com.navercorp.pinpoint.web.alarm.checker.AlarmChecker;
import com.navercorp.pinpoint.web.service.UserGroupService;
import org.apache.hadoop.hbase.shaded.org.apache.http.HttpResponse;
import org.apache.hadoop.hbase.shaded.org.apache.http.HttpStatus;
import org.apache.hadoop.hbase.shaded.org.apache.http.client.HttpClient;
import org.apache.hadoop.hbase.shaded.org.apache.http.client.methods.HttpPost;
import org.apache.hadoop.hbase.shaded.org.apache.http.entity.StringEntity;
import org.apache.hadoop.hbase.shaded.org.apache.http.impl.client.DefaultHttpClient;
import org.apache.hadoop.hbase.shaded.org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailSender;

import java.util.List;
//@Service
//@Transactional(rollbackFor = {Exception.class})
public class AlarmMessageSenderImple implements AlarmMessageSender {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private MailSender mailSender;

    public void setMailSender(MailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Autowired
    private UserGroupService userGroupService;

    @Override
    public void sendSms(AlarmChecker checker, int sequenceCount) {
        List<String> receivers = userGroupService.selectPhoneNumberOfMember(checker.getuserGroupId());

        if (receivers.size() == 0) {
            return;
        }

        for (Object message : checker.getSmsMessage()) {
//            logger.info(" ------------------------------ send SMS : {} ------------------------------ ", message);

            // TODO Implement logic for sending SMS
        }
    }

    @Override
    public void sendEmail(AlarmChecker checker, int sequenceCount) {
        List<String> receivers = userGroupService.selectEmailOfMember(checker.getuserGroupId());

        logger.info(" ------------------------------ send to ding ding Robot : {} ------------------------------ ", checker.getEmailMessage());
        if (receivers.size() == 0) {
            return;
        }

        try{
            String content = String.format("[PINPOINT Alarm - %s] %s\n\n%s",checker.getRule().getApplicationId(),checker.getEmailMessage(), checker.getRule().getNotes());
            //内容
            sendDingTalk(content,checker.getRule().getUserGroupId());

//            SimpleMailMessage message = new SimpleMailMessage();
//            message.setTo(receivers.toArray(new String [receivers.size()]));
//            message.setSubject(String.format("[PINPOINT Alarm - %s]", checker.getRule().getApplicationId()));
//            message.setText(content);
//            mailSender.send(message);

        }catch(Exception e){
            logger.error("send email error", e);
        }
    }


    private void sendDingTalk(String content,String prefix) throws Exception{

        logger.info(" ------------------------------ prefix:{}",prefix);
        //pinpoint测试-Robot
        String WEBHOOK_TOKEN = "https://oapi.dingtalk.com/robot/send?access_token=fc72677905adfc10562349ee6a10ccdf38dc17825556732f78e1fdf6c0f6b50c";
        //TODO 后期可以改成给每个用户组配置一个WEBHOOK 的方式，实现不同应用的消息推送到不同的群
        //开发环境
        if("dev-".startsWith(prefix.toLowerCase())){
            WEBHOOK_TOKEN = "https://oapi.dingtalk.com/robot/send?access_token=fc72677905adfc10562349ee6a10ccdf38dc17825556732f78e1fdf6c0f6b50c";
        }
        //qa 环境
        else if("qa-".startsWith(prefix.toLowerCase())){
            WEBHOOK_TOKEN = "https://oapi.dingtalk.com/robot/send?access_token=b050626246d284acd3ad79f3a13be15609dce59ede479d8690972795a91c3c50";
        }

        HttpClient httpclient = new DefaultHttpClient();

        HttpPost httppost = new HttpPost(WEBHOOK_TOKEN);
        httppost.addHeader("Content-Type", "application/json; charset=utf-8");

        String textMsg = "{ \"msgtype\": \"text\", \"text\": {\"content\": \"" + content + "\"}}";
        StringEntity se = new StringEntity(textMsg, "utf-8");
        httppost.setEntity(se);

        HttpResponse response = httpclient.execute(httppost);
        if (response.getStatusLine().getStatusCode()== HttpStatus.SC_OK){
            String result= EntityUtils.toString(response.getEntity(), "utf-8");
            System.out.println(result);
        }
    }

//    public static void main(String args[]){
//
//        try
//        {
//            sendDingTalk("测试信息："+System.currentTimeMillis());
//        }
//        catch (Exception e)
//        {
//            e.printStackTrace();
//        }
//
//    }
}