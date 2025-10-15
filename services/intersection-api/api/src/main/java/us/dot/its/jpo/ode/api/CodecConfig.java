package us.dot.its.jpo.ode.api;

import j2735ffm.MessageFrameCodec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CodecConfig {

    @Value("${j2735.api.text-buffer-size}")
    private long textBufferSize;

    @Value("${j2735.api.uper-buffer-size}")
    private long uperBufferSize;

    @Value("${j2735.api.message-frame-allocate-size}")
    private long messageFrameAllocateSize;

    @Value("${j2735.api.asn-codec-ctx-max-stack-size}")
    private long asnCodecCtxMaxStackSize;

    @Bean
    public MessageFrameCodec messageFrameCodec() {
        return new MessageFrameCodec(
                textBufferSize,
                uperBufferSize,
                messageFrameAllocateSize,
                asnCodecCtxMaxStackSize);
    }
}
