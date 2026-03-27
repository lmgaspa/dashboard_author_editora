package billing

import (
	"context"
	"database/sql"
	"time"

	_ "github.com/lib/pq"
)

// User represents a user with an author_id.
type User struct {
	ID       int
	AuthorID string
	Name     string
	Email    string
}

// MonthlyCharge represents a monthly charge record.
type MonthlyCharge struct {
	ID       int
	AuthorID string
	Month    int
	Year     int
	Amount   float64
	Status   string
	DueDate  time.Time
}

// Repository handles PostgreSQL queries for billing.
type Repository struct {
	db *sql.DB
}

// NewRepository creates a new billing Repository.
func NewRepository(db *sql.DB) *Repository {
	return &Repository{db: db}
}

// FindUsersWithAuthorId returns all users that have an author_id.
func (r *Repository) FindUsersWithAuthorId(ctx context.Context) ([]User, error) {
	rows, err := r.db.QueryContext(ctx, `
		SELECT id, author_id, name, email
		FROM users
		WHERE author_id IS NOT NULL
		  AND author_id <> ''
	`)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var users []User
	for rows.Next() {
		var u User
		if err := rows.Scan(&u.ID, &u.AuthorID, &u.Name, &u.Email); err != nil {
			return nil, err
		}
		users = append(users, u)
	}
	return users, rows.Err()
}

// FindOverdueCharges returns charges that are OVERDUE or PENDING past their due date.
func (r *Repository) FindOverdueCharges(ctx context.Context) ([]MonthlyCharge, error) {
	rows, err := r.db.QueryContext(ctx, `
		SELECT id, author_id, charge_month, charge_year, amount, status, due_date
		FROM monthly_charges
		WHERE status = 'OVERDUE'
		   OR (status = 'PENDING' AND due_date < NOW())
	`)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var charges []MonthlyCharge
	for rows.Next() {
		var c MonthlyCharge
		if err := rows.Scan(&c.ID, &c.AuthorID, &c.Month, &c.Year, &c.Amount, &c.Status, &c.DueDate); err != nil {
			return nil, err
		}
		charges = append(charges, c)
	}
	return charges, rows.Err()
}

// FindAdminEmails returns email addresses of all ADMIN users.
func (r *Repository) FindAdminEmails(ctx context.Context) ([]string, error) {
	rows, err := r.db.QueryContext(ctx, `
		SELECT email
		FROM users
		WHERE role = 'ADMIN'
	`)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var emails []string
	for rows.Next() {
		var email string
		if err := rows.Scan(&email); err != nil {
			return nil, err
		}
		emails = append(emails, email)
	}
	return emails, rows.Err()
}

// ExistsTicketForCharge checks whether a support ticket already exists for the given author + charge.
func (r *Repository) ExistsTicketForCharge(ctx context.Context, authorID string, chargeID int) (bool, error) {
	var count int
	err := r.db.QueryRowContext(ctx, `
		SELECT COUNT(1)
		FROM tickets
		WHERE author_id = $1
		  AND charge_id = $2
	`, authorID, chargeID).Scan(&count)
	if err != nil {
		return false, err
	}
	return count > 0, nil
}

// ChargeExistsForMonth checks whether a charge was already created for the given author/month/year.
func (r *Repository) ChargeExistsForMonth(ctx context.Context, authorID string, month, year int) (bool, error) {
	var count int
	err := r.db.QueryRowContext(ctx, `
		SELECT COUNT(1)
		FROM monthly_charges
		WHERE author_id = $1
		  AND charge_month = $2
		  AND charge_year  = $3
	`, authorID, month, year).Scan(&count)
	if err != nil {
		return false, err
	}
	return count > 0, nil
}
