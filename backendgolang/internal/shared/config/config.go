package config

import (
	"strings"

	"github.com/spf13/viper"
)

type Config struct {
	RabbitMQURL     string `mapstructure:"RABBITMQ_URL"`
	MongoDBURL      string `mapstructure:"MONGODB_URL"`
	MongoDB         string `mapstructure:"MONGODB_DB"`
	RedisURL        string `mapstructure:"REDIS_URL"`
	SMTPHost        string `mapstructure:"SMTP_HOST"`
	SMTPPort        int    `mapstructure:"SMTP_PORT"`
	SMTPUsername    string `mapstructure:"SMTP_USERNAME"`
	SMTPPassword    string `mapstructure:"SMTP_PASSWORD"`
	SMTPFrom        string `mapstructure:"SMTP_FROM"`
	PostgresURL     string `mapstructure:"POSTGRES_URL"`
	GRPCPort        int    `mapstructure:"GRPC_PORT"`
	FrontendBaseURL string `mapstructure:"FRONTEND_BASE_URL"`
	MetricsPort     int    `mapstructure:"METRICS_PORT"`
	OTELEnabled     bool   `mapstructure:"OTEL_ENABLED"`
}

func Load() (*Config, error) {
	viper.SetConfigFile(".env")
	viper.SetConfigType("env")
	viper.AutomaticEnv()
	viper.SetEnvKeyReplacer(strings.NewReplacer(".", "_"))

	// defaults
	viper.SetDefault("RABBITMQ_URL", "amqp://guest:guest@localhost:5672/")
	viper.SetDefault("MONGODB_URL", "mongodb://localhost:27017")
	viper.SetDefault("MONGODB_DB", "painel_autor")
	viper.SetDefault("REDIS_URL", "redis://localhost:6379")
	viper.SetDefault("SMTP_HOST", "smtp.gmail.com")
	viper.SetDefault("SMTP_PORT", 587)
	viper.SetDefault("GRPC_PORT", 50051)
	viper.SetDefault("FRONTEND_BASE_URL", "https://painel.andeseditora.com.br")
	viper.SetDefault("METRICS_PORT", 2112)
	viper.SetDefault("OTEL_ENABLED", true)

	// ignore missing .env file; env vars still work
	_ = viper.ReadInConfig()

	var cfg Config
	if err := viper.Unmarshal(&cfg); err != nil {
		return nil, err
	}
	return &cfg, nil
}
