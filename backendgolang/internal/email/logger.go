package email

import (
	"context"
	"time"

	"go.mongodb.org/mongo-driver/v2/bson"
	"go.mongodb.org/mongo-driver/v2/mongo"
)

// EmailLog represents a log entry for a sent (or failed) email.
type EmailLog struct {
	ID        bson.ObjectID `bson:"_id,omitempty"`
	EventType string        `bson:"event_type"`
	To        string        `bson:"to"`
	Subject   string        `bson:"subject"`
	Status    string        `bson:"status"` // "sent" | "failed"
	Error     string        `bson:"error,omitempty"`
	SentAt    time.Time     `bson:"sent_at"`
}

// MongoLogger logs email events to MongoDB.
type MongoLogger struct {
	col *mongo.Collection
}

// NewMongoLogger creates a MongoLogger using the given database.
func NewMongoLogger(db *mongo.Database) *MongoLogger {
	return &MongoLogger{col: db.Collection("email_logs")}
}

// Log inserts an EmailLog document into the collection.
func (l *MongoLogger) Log(ctx context.Context, entry EmailLog) error {
	entry.ID = bson.NewObjectID()
	if entry.SentAt.IsZero() {
		entry.SentAt = time.Now().UTC()
	}
	_, err := l.col.InsertOne(ctx, entry)
	return err
}
