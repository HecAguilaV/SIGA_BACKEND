package com.siga.backend.config

import org.springframework.beans.BeansException
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.stereotype.Component

@Component
class ApplicationContextProvider : ApplicationContextAware {
    
    companion object {
        @Volatile
        private var applicationContext: ApplicationContext? = null
        
        fun <T> getBean(beanClass: Class<T>): T {
            val context = applicationContext
                ?: throw IllegalStateException("ApplicationContext no está inicializado")
            return context.getBean(beanClass)
        }
    }
    
    @Throws(BeansException::class)
    override fun setApplicationContext(context: ApplicationContext) {
        applicationContext = context
    }
}
