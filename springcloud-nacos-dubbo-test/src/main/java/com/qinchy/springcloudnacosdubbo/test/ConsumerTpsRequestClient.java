package com.qinchy.springcloudnacosdubbo.test;

import com.qinchy.springcloudnacosdubbo.test.utils.HttpClientUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;

/**
 * 用于压测消费端的Tps
 *
 * @author Administrator
 */
@Slf4j
public class ConsumerTpsRequestClient extends AbstractJavaSamplerClient {
    @Override
    public void setupTest(JavaSamplerContext context) {
        super.setupTest(context);
    }

    @Override
    public void teardownTest(JavaSamplerContext context) {
        super.teardownTest(context);
    }

    @Override
    public Arguments getDefaultParameters() {
        Arguments args = new Arguments();
        args.addArgument("url", "http://localhost:9091/people/selectAll");
        return args;
    }

    @Override
    public SampleResult runTest(JavaSamplerContext javaSamplerContext) {
        SampleResult result = new SampleResult();
        result.setSamplerData("");
        result.sampleStart();
        try {
            String response = HttpClientUtils.invokeGet(javaSamplerContext.getParameter("url"), null, 10000);
            result.setResponseData(response, "utf-8");
            result.setDataType(SampleResult.TEXT);
            result.setSuccessful(true);
        } catch (Exception e) {
            result.setSuccessful(false);
            log.error("请求报错", e);
        } finally {
            result.sampleEnd();
        }

        return result;
    }
}
